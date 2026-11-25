package com.example.usermgmt.logservice.service;

import com.example.usermgmt.common.api.PageResponse;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.logservice.config.LogServiceProperties;
import com.example.usermgmt.logservice.dto.AccessLogQueryRequest;
import com.example.usermgmt.logservice.dto.AccessLogRecord;
import com.example.usermgmt.logservice.dto.MetricPoint;
import com.example.usermgmt.logservice.dto.MetricQueryRequest;
import com.example.usermgmt.logservice.dto.MetricSeriesResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LogAnalysisService {

    private static final DateTimeFormatter SQL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final LogServiceProperties properties;
    private final Optional<JdbcTemplate> jdbcTemplate;

    public LogAnalysisService(
        LogServiceProperties properties,
        @Qualifier("clickHouseJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider
    ) {
        this.properties = properties;
        this.jdbcTemplate = Optional.ofNullable(jdbcTemplateProvider.getIfAvailable());
    }

    public PageResponse<AccessLogRecord> queryAccessLogs(AccessLogQueryRequest request) {
        TimeRange range = validateRange(request.getStartTime(), request.getEndTime());
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        if (useMockData()) {
            List<AccessLogRecord> rows = mockLogs(range).stream()
                .filter(row -> matchesLogFilter(row, request))
                .sorted(Comparator.comparing(AccessLogRecord::getTimestamp).reversed())
                .toList();
            return paginate(rows, page, pageSize);
        }

        QueryParts queryParts = buildAccessLogWhere(request, range);
        List<Object> params = new ArrayList<>(queryParts.params());
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        String tableName = safeTableName();
        String listSql = """
            SELECT timestamp, microservice, api_endpoint, http_method, status_code, latency_ms, trace_id, client_ip, user_agent, user_id
            FROM %s
            WHERE %s
            ORDER BY timestamp DESC
            LIMIT ? OFFSET ?
            """.formatted(tableName, queryParts.whereSql());
        String countSql = "SELECT count() FROM %s WHERE %s".formatted(tableName, queryParts.whereSql());

        JdbcTemplate clickHouse = requireClickHouse();
        List<AccessLogRecord> rows = clickHouse.query(listSql, this::mapAccessLogRecord, params.toArray());
        Long total = clickHouse.queryForObject(countSql, Long.class, queryParts.params().toArray());
        return PageResponse.<AccessLogRecord>builder()
            .page(page)
            .pageSize(pageSize)
            .total(total == null ? 0 : total)
            .list(rows)
            .build();
    }

    public MetricSeriesResponse requestRate(MetricQueryRequest request) {
        return buildMetric("request_rate", "qps", request, this::queryRequestRate);
    }

    public MetricSeriesResponse timeoutRate(MetricQueryRequest request) {
        return buildMetric("request_timeout_rate", "percent", request, this::queryTimeoutRate);
    }

    public MetricSeriesResponse errorRate(MetricQueryRequest request) {
        return buildMetric("request_error_rate", "percent", request, this::queryErrorRate);
    }

    public MetricSeriesResponse latency(MetricQueryRequest request) {
        return buildMetric("request_latency", "ms", request, this::queryLatency);
    }

    public MetricSeriesResponse downstreamErrorRate(MetricQueryRequest request) {
        return buildMetric("downstream_error_rate", "percent", request, this::queryDownstreamErrorRate);
    }

    public MetricSeriesResponse downstreamLatency(MetricQueryRequest request) {
        return buildMetric("downstream_latency", "ms", request, this::queryDownstreamLatency);
    }

    public MetricSeriesResponse ipRequestRate(MetricQueryRequest request) {
        return buildMetric("ip_request_rate", "qps", request, this::queryIpRequestRate);
    }

    public MetricSeriesResponse userRequestRate(MetricQueryRequest request) {
        return buildMetric("user_request_rate", "qps", request, this::queryUserRequestRate);
    }

    private MetricSeriesResponse buildMetric(String metricName, String unit, MetricQueryRequest request, MetricQueryFunction queryFunction) {
        TimeRange range = validateRange(request.getStartTime(), request.getEndTime());
        long windowSeconds = parseWindowSeconds(request.getWindowSize());
        List<MetricPoint> points = useMockData()
            ? mockMetric(metricName, unit, request, range, windowSeconds)
            : queryFunction.query(request, range, windowSeconds);
        return MetricSeriesResponse.builder()
            .metricName(metricName)
            .unit(unit)
            .windowSize(request.getWindowSize())
            .points(points)
            .build();
    }

    private List<MetricPoint> queryRequestRate(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        QueryParts filters = buildBaseMetricWhere(request, range);
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   count() AS total
            FROM %s
            WHERE %s
            GROUP BY bucket_start
            ORDER BY bucket_start
            """.formatted(windowSeconds, safeTableName(), filters.whereSql());
        return requireClickHouse().query(sql, (rs, rowNum) -> metricPointFromBucket(rs, windowSeconds, request, rs.getLong("total") / (double) windowSeconds, rs.getLong("total"), null), filters.params().toArray());
    }

    private List<MetricPoint> queryTimeoutRate(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        QueryParts filters = buildBaseMetricWhere(request, range);
        long timeoutThreshold = request.getTimeoutThresholdMs() == null ? properties.getDefaultTimeoutThresholdMs() : request.getTimeoutThresholdMs();
        List<Object> params = new ArrayList<>(filters.params());
        params.add(timeoutThreshold);
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   count() AS total,
                   countIf(latency_ms > ?) AS matched
            FROM %s
            WHERE %s
            GROUP BY bucket_start
            ORDER BY bucket_start
            """.formatted(windowSeconds, safeTableName(), filters.whereSql());
        return requireClickHouse().query(sql, (rs, rowNum) -> ratePointFromBucket(rs, windowSeconds, request), params.toArray());
    }

    private List<MetricPoint> queryErrorRate(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        QueryParts filters = buildBaseMetricWhere(request, range);
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   count() AS total,
                   countIf(status_code >= 500) AS matched
            FROM %s
            WHERE %s
            GROUP BY bucket_start
            ORDER BY bucket_start
            """.formatted(windowSeconds, safeTableName(), filters.whereSql());
        return requireClickHouse().query(sql, (rs, rowNum) -> ratePointFromBucket(rs, windowSeconds, request), filters.params().toArray());
    }

    private List<MetricPoint> queryLatency(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        QueryParts filters = buildBaseMetricWhere(request, range);
        String quantile = quantileFunction(request.getPercentile());
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   count() AS total,
                   %s(latency_ms) AS value
            FROM %s
            WHERE %s
            GROUP BY bucket_start
            ORDER BY bucket_start
            """.formatted(windowSeconds, quantile, safeTableName(), filters.whereSql());
        return requireClickHouse().query(sql, (rs, rowNum) -> metricPointFromBucket(rs, windowSeconds, request, rs.getDouble("value"), rs.getLong("total"), null), filters.params().toArray());
    }

    private List<MetricPoint> queryDownstreamErrorRate(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        QueryParts filters = buildDownstreamWhere(request, range);
        String errorExpression = downstreamErrorExpression(request.getErrorType());
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   downstream_service,
                   count() AS total,
                   countIf(%s) AS matched
            FROM %s
            WHERE %s
            GROUP BY bucket_start, downstream_service
            ORDER BY bucket_start
            """.formatted(windowSeconds, errorExpression, safeTableName(), filters.whereSql());
        return requireClickHouse().query(sql, (rs, rowNum) -> {
            MetricPoint point = ratePointFromBucket(rs, windowSeconds, request);
            point.setDownstreamService(rs.getString("downstream_service"));
            point.setGroupKey(rs.getString("downstream_service"));
            return point;
        }, filters.params().toArray());
    }

    private List<MetricPoint> queryDownstreamLatency(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        QueryParts filters = buildDownstreamWhere(request, range);
        String quantile = quantileFunction(request.getPercentile());
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   downstream_service,
                   count() AS total,
                   %s(downstream_duration_ms) AS value
            FROM %s
            WHERE %s
            GROUP BY bucket_start, downstream_service
            ORDER BY bucket_start
            """.formatted(windowSeconds, quantile, safeTableName(), filters.whereSql());
        return requireClickHouse().query(sql, (rs, rowNum) -> {
            MetricPoint point = metricPointFromBucket(rs, windowSeconds, request, rs.getDouble("value"), rs.getLong("total"), null);
            point.setDownstreamService(rs.getString("downstream_service"));
            point.setGroupKey(rs.getString("downstream_service"));
            return point;
        }, filters.params().toArray());
    }

    private List<MetricPoint> queryIpRequestRate(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        return queryGroupedQps(request, range, windowSeconds, "client_ip", request.getClientIp(), request.getMinQps());
    }

    private List<MetricPoint> queryUserRequestRate(MetricQueryRequest request, TimeRange range, long windowSeconds) {
        return queryGroupedQps(request, range, windowSeconds, "user_id", request.getUserId(), request.getMinQps());
    }

    private List<MetricPoint> queryGroupedQps(
        MetricQueryRequest request,
        TimeRange range,
        long windowSeconds,
        String groupColumn,
        String exactValue,
        Double minQps
    ) {
        QueryParts filters = buildBaseMetricWhere(request, range);
        List<String> where = new ArrayList<>();
        where.add(filters.whereSql());
        List<Object> params = new ArrayList<>(filters.params());
        if (StringUtils.hasText(exactValue)) {
            where.add(groupColumn + " = ?");
            params.add(exactValue);
        } else {
            where.add(groupColumn + " != ''");
        }
        String havingSql = minQps == null ? "" : " HAVING value >= ? ";
        if (minQps != null) {
            params.add(minQps);
        }
        String sql = """
            SELECT toStartOfInterval(timestamp, INTERVAL %d SECOND) AS bucket_start,
                   %s AS group_value,
                   count() AS total,
                   count() / %d AS value
            FROM %s
            WHERE %s
            GROUP BY bucket_start, group_value
            %s
            ORDER BY bucket_start, value DESC
            """.formatted(windowSeconds, groupColumn, windowSeconds, safeTableName(), String.join(" AND ", where), havingSql);
        return requireClickHouse().query(sql, (rs, rowNum) -> {
            MetricPoint point = metricPointFromBucket(rs, windowSeconds, request, rs.getDouble("value"), rs.getLong("total"), null);
            String groupValue = rs.getString("group_value");
            point.setGroupKey(groupValue);
            if ("client_ip".equals(groupColumn)) {
                point.setClientIp(groupValue);
            } else {
                point.setUserId(groupValue);
            }
            return point;
        }, params.toArray());
    }

    private QueryParts buildAccessLogWhere(AccessLogQueryRequest request, TimeRange range) {
        List<String> where = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        addTimeRange(where, params, range);
        addEquals(where, params, "microservice", request.getMicroservice());
        addEquals(where, params, "api_endpoint", request.getApiEndpoint());
        addEquals(where, params, "http_method", request.getHttpMethod());
        addEquals(where, params, "status_code", request.getStatusCode());
        addEquals(where, params, "client_ip", request.getClientIp());
        addEquals(where, params, "trace_id", request.getTraceId());
        addEquals(where, params, "user_id", request.getUserId());
        return new QueryParts(String.join(" AND ", where), params);
    }

    private QueryParts buildBaseMetricWhere(MetricQueryRequest request, TimeRange range) {
        List<String> where = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        addTimeRange(where, params, range);
        addEquals(where, params, "microservice", request.getService());
        addEquals(where, params, "api_endpoint", request.getApiEndpoint());
        return new QueryParts(String.join(" AND ", where), params);
    }

    private QueryParts buildDownstreamWhere(MetricQueryRequest request, TimeRange range) {
        List<String> where = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        addTimeRange(where, params, range);
        addEquals(where, params, "microservice", request.getService());
        addEquals(where, params, "downstream_service", request.getDownstreamService());
        where.add("downstream_service != ''");
        return new QueryParts(String.join(" AND ", where), params);
    }

    private void addTimeRange(List<String> where, List<Object> params, TimeRange range) {
        where.add("timestamp >= toDateTime64(?, 3, 'UTC')");
        params.add(sqlTime(range.start()));
        where.add("timestamp <= toDateTime64(?, 3, 'UTC')");
        params.add(sqlTime(range.end()));
    }

    private void addEquals(List<String> where, List<Object> params, String column, Object value) {
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return;
        }
        if (value != null) {
            where.add(column + " = ?");
            params.add(value);
        }
    }

    private AccessLogRecord mapAccessLogRecord(ResultSet rs, int rowNum) throws SQLException {
        return AccessLogRecord.builder()
            .timestamp(formatResponseTime(readInstant(rs, "timestamp")))
            .microservice(rs.getString("microservice"))
            .apiEndpoint(rs.getString("api_endpoint"))
            .httpMethod(rs.getString("http_method"))
            .statusCode(rs.getInt("status_code"))
            .latencyMs(rs.getLong("latency_ms"))
            .traceId(rs.getString("trace_id"))
            .clientIp(rs.getString("client_ip"))
            .userAgent(rs.getString("user_agent"))
            .userId(rs.getString("user_id"))
            .build();
    }

    private MetricPoint metricPointFromBucket(ResultSet rs, long windowSeconds, MetricQueryRequest request, Double value, Long total, Long matched)
        throws SQLException {
        Instant bucketStart = readInstant(rs, "bucket_start");
        Instant bucketEnd = bucketStart.plusSeconds(windowSeconds);
        return MetricPoint.builder()
            .bucketStart(formatResponseTime(bucketStart))
            .bucketEnd(formatResponseTime(bucketEnd))
            .service(request.getService())
            .apiEndpoint(request.getApiEndpoint())
            .downstreamService(request.getDownstreamService())
            .clientIp(request.getClientIp())
            .userId(request.getUserId())
            .value(value)
            .total(total)
            .matched(matched)
            .build();
    }

    private MetricPoint ratePointFromBucket(ResultSet rs, long windowSeconds, MetricQueryRequest request) throws SQLException {
        long total = rs.getLong("total");
        long matched = rs.getLong("matched");
        double value = total == 0 ? 0D : matched * 100D / total;
        return metricPointFromBucket(rs, windowSeconds, request, value, total, matched);
    }

    private Instant readInstant(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        return parseTime(Objects.toString(value));
    }

    private TimeRange validateRange(String startTime, String endTime) {
        Instant start = parseTime(startTime);
        Instant end = parseTime(endTime);
        if (!start.isBefore(end)) {
            throw new BusinessException(400, "start_time must be before end_time");
        }
        long maxDays = properties.getMaxQueryRangeDays();
        if (Duration.between(start, end).toDays() > maxDays) {
            throw new BusinessException(400, "查询时间范围不能超过 " + maxDays + " 天");
        }
        return new TimeRange(start, end);
    }

    private Instant parseTime(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            throw new BusinessException(400, "时间不能为空");
        }
        String value = rawValue.trim();
        try {
            return Instant.parse(value);
        } catch (RuntimeException ignored) {
            // Try other common input shapes below.
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (RuntimeException ignored) {
            // Try local date-time below.
        }
        try {
            return LocalDateTime.parse(value.replace(" ", "T")).toInstant(ZoneOffset.UTC);
        } catch (RuntimeException exception) {
            throw new BusinessException(400, "时间格式必须为 ISO8601，例如 2025-05-26T00:00:00Z");
        }
    }

    private long parseWindowSeconds(String windowSize) {
        if (!StringUtils.hasText(windowSize)) {
            throw new BusinessException(400, "window_size is required");
        }
        String value = windowSize.trim().toLowerCase(Locale.ROOT);
        try {
            long amount = Long.parseLong(value.substring(0, value.length() - 1));
            String unit = value.substring(value.length() - 1);
            long seconds = switch (unit) {
                case "s" -> amount;
                case "m" -> amount * 60;
                case "h" -> amount * 3600;
                case "d" -> amount * 86400;
                default -> throw new BusinessException(400, "window_size only supports s/m/h/d");
            };
            if (seconds <= 0 || seconds > 86400 * 7L) {
                throw new BusinessException(400, "window_size must be between 1s and 7d");
            }
            return seconds;
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            throw new BusinessException(400, "window_size format example: 1h, 1d");
        }
    }

    private String quantileFunction(String percentile) {
        String normalized = StringUtils.hasText(percentile) ? percentile.toLowerCase(Locale.ROOT) : "p99";
        return switch (normalized) {
            case "p50" -> "quantile(0.50)";
            case "p95" -> "quantile(0.95)";
            case "p99" -> "quantile(0.99)";
            default -> throw new BusinessException(400, "percentile only supports p50, p95, p99");
        };
    }

    private String downstreamErrorExpression(String errorType) {
        if (!StringUtils.hasText(errorType)) {
            return "downstream_status_code >= 500 OR downstream_error_type IN ('timeout', 'connection_failure')";
        }
        return switch (errorType.toLowerCase(Locale.ROOT)) {
            case "5xx" -> "downstream_status_code >= 500";
            case "timeout" -> "downstream_error_type = 'timeout'";
            case "connection_failure" -> "downstream_error_type = 'connection_failure'";
            default -> throw new BusinessException(400, "error_type only supports 5xx, timeout, connection_failure");
        };
    }

    private boolean useMockData() {
        return properties.isMockEnabled();
    }

    private JdbcTemplate requireClickHouse() {
        return jdbcTemplate.orElseThrow(() -> new BusinessException(503, "ClickHouse 未启用或 JDBC 未初始化，请检查 log-service.clickhouse.enabled 配置"));
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        int value = pageSize == null || pageSize < 1 ? properties.getDefaultPageSize() : pageSize;
        return Math.min(value, properties.getMaxPageSize());
    }

    private <T> PageResponse<T> paginate(List<T> rows, int page, int pageSize) {
        int from = Math.min((page - 1) * pageSize, rows.size());
        int to = Math.min(from + pageSize, rows.size());
        return PageResponse.<T>builder()
            .page(page)
            .pageSize(pageSize)
            .total((long) rows.size())
            .list(rows.subList(from, to))
            .build();
    }

    private String safeTableName() {
        String tableName = properties.getTableName();
        if (!StringUtils.hasText(tableName) || !tableName.matches("[A-Za-z0-9_\\.]+")) {
            throw new BusinessException(500, "非法 ClickHouse 表名配置");
        }
        return tableName;
    }

    private String sqlTime(Instant instant) {
        return SQL_TIME_FORMATTER.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private String formatResponseTime(Instant instant) {
        return RESPONSE_TIME_FORMATTER.format(instant.atOffset(ZoneOffset.UTC));
    }

    private List<AccessLogRecord> mockLogs(TimeRange range) {
        List<String> services = List.of("topbiz", "user-service", "notification-service", "log-service");
        List<String> apis = List.of("/api/v1/login", "/api/v1/users", "/api/v1/messages/send", "/api/v1/admin/logs/query");
        List<AccessLogRecord> rows = new ArrayList<>();
        long spanSeconds = Math.max(1, Duration.between(range.start(), range.end()).toSeconds());
        for (int i = 0; i < 32; i++) {
            Instant timestamp = range.start().plusSeconds(spanSeconds * i / 32);
            int status = i % 11 == 0 ? 500 : i % 7 == 0 ? 404 : 200;
            rows.add(AccessLogRecord.builder()
                .timestamp(formatResponseTime(timestamp))
                .microservice(services.get(i % services.size()))
                .apiEndpoint(apis.get(i % apis.size()))
                .httpMethod(i % 3 == 0 ? "POST" : "GET")
                .statusCode(status)
                .latencyMs(35L + i * 17L)
                .traceId("trace-" + (1000 + i))
                .clientIp("192.168.1." + (10 + i % 6))
                .userAgent("Mozilla/5.0 mock-log-service")
                .userId(i % 4 == 0 ? "" : "user-" + (i % 5 + 1))
                .build());
        }
        return rows;
    }

    private boolean matchesLogFilter(AccessLogRecord row, AccessLogQueryRequest request) {
        return equalsIfPresent(row.getMicroservice(), request.getMicroservice())
            && equalsIfPresent(row.getApiEndpoint(), request.getApiEndpoint())
            && equalsIfPresent(row.getHttpMethod(), request.getHttpMethod())
            && (request.getStatusCode() == null || request.getStatusCode().equals(row.getStatusCode()))
            && equalsIfPresent(row.getClientIp(), request.getClientIp())
            && equalsIfPresent(row.getTraceId(), request.getTraceId())
            && equalsIfPresent(row.getUserId(), request.getUserId());
    }

    private boolean equalsIfPresent(String actual, String expected) {
        return !StringUtils.hasText(expected) || expected.equalsIgnoreCase(Objects.toString(actual, ""));
    }

    private List<MetricPoint> mockMetric(String metricName, String unit, MetricQueryRequest request, TimeRange range, long windowSeconds) {
        List<MetricPoint> points = new ArrayList<>();
        Instant cursor = range.start();
        int index = 0;
        while (cursor.isBefore(range.end()) && points.size() < 200) {
            Instant bucketEnd = cursor.plusSeconds(windowSeconds);
            double base = 10 + Math.abs(Objects.hash(metricName, request.getService(), index) % 30);
            double value = switch (unit) {
                case "percent" -> Math.min(100D, base / 2D);
                case "ms" -> 80D + base * 12D;
                default -> base / Math.max(1D, windowSeconds / 60D);
            };
            Long total = 120L + index * 3L;
            Long matched = "percent".equals(unit) ? Math.round(total * value / 100D) : null;
            MetricPoint point = MetricPoint.builder()
                .bucketStart(formatResponseTime(cursor))
                .bucketEnd(formatResponseTime(bucketEnd.isAfter(range.end()) ? range.end() : bucketEnd))
                .service(request.getService())
                .apiEndpoint(request.getApiEndpoint())
                .downstreamService(request.getDownstreamService())
                .clientIp(request.getClientIp())
                .userId(request.getUserId())
                .groupKey(resolveMockGroupKey(metricName, request, index))
                .value(round(value))
                .total(total)
                .matched(matched)
                .build();
            if (metricName.equals("ip_request_rate")) {
                point.setClientIp(StringUtils.hasText(request.getClientIp()) ? request.getClientIp() : "192.168.1." + (10 + index % 5));
                point.setGroupKey(point.getClientIp());
            }
            if (metricName.equals("user_request_rate")) {
                point.setUserId(StringUtils.hasText(request.getUserId()) ? request.getUserId() : "user-" + (index % 5 + 1));
                point.setGroupKey(point.getUserId());
            }
            if (request.getMinQps() == null || !"qps".equals(unit) || point.getValue() >= request.getMinQps()) {
                points.add(point);
            }
            cursor = bucketEnd;
            index++;
        }
        return points;
    }

    private String resolveMockGroupKey(String metricName, MetricQueryRequest request, int index) {
        if (metricName.startsWith("downstream")) {
            return StringUtils.hasText(request.getDownstreamService()) ? request.getDownstreamService() : "user-service";
        }
        if (StringUtils.hasText(request.getApiEndpoint())) {
            return request.getApiEndpoint();
        }
        return String.valueOf(index);
    }

    private Double round(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private record TimeRange(Instant start, Instant end) {
    }

    private record QueryParts(String whereSql, List<Object> params) {
    }

    @FunctionalInterface
    private interface MetricQueryFunction {
        List<MetricPoint> query(MetricQueryRequest request, TimeRange range, long windowSeconds);
    }
}
