package com.example.usermgmt.logservice.service;

import com.example.usermgmt.common.api.PageResponse;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.logservice.config.LogServiceProperties;
import com.example.usermgmt.logservice.dto.AlertRuleRequest;
import com.example.usermgmt.logservice.dto.AlertRuleResponse;
import com.example.usermgmt.logservice.dto.AlertTriggerRecord;
import com.example.usermgmt.logservice.dto.MetricPoint;
import com.example.usermgmt.logservice.dto.MetricQueryRequest;
import com.example.usermgmt.logservice.dto.MetricSeriesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AlertRuleService {

    private static final Logger log = LoggerFactory.getLogger(AlertRuleService.class);
    private static final List<String> SUPPORTED_METRICS = List.of(
        "request_rate",
        "request_timeout_rate",
        "request_error_rate",
        "request_latency",
        "downstream_error_rate",
        "downstream_latency",
        "ip_request_rate",
        "user_request_rate"
    );

    private final JdbcTemplate alertJdbcTemplate;
    private final LogAnalysisService logAnalysisService;
    private final LogServiceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AlertRuleService(
        @Qualifier("alertJdbcTemplate") JdbcTemplate alertJdbcTemplate,
        LogAnalysisService logAnalysisService,
        LogServiceProperties properties,
        ObjectMapper objectMapper
    ) {
        this.alertJdbcTemplate = alertJdbcTemplate;
        this.logAnalysisService = logAnalysisService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @PostConstruct
    public void initializeSchema() {
        alertJdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS alert_rule (
                id VARCHAR(64) PRIMARY KEY,
                rule_name VARCHAR(128) NOT NULL,
                metric_type VARCHAR(64) NOT NULL,
                service VARCHAR(128),
                api_endpoint VARCHAR(512),
                downstream_service VARCHAR(128),
                error_type VARCHAR(64),
                percentile VARCHAR(16),
                client_ip VARCHAR(64),
                user_id VARCHAR(128),
                timeout_threshold_ms BIGINT,
                window_size VARCHAR(32) NOT NULL,
                comparator VARCHAR(8) NOT NULL,
                threshold DOUBLE NOT NULL,
                consecutive_times INT NOT NULL,
                notification_channel VARCHAR(32),
                notification_target VARCHAR(512),
                enabled BOOLEAN NOT NULL,
                created_time TIMESTAMP NOT NULL,
                updated_time TIMESTAMP NOT NULL,
                last_evaluated_at TIMESTAMP,
                last_metric_value DOUBLE,
                current_consecutive_count INT DEFAULT 0,
                trigger_count INT DEFAULT 0,
                last_triggered_at TIMESTAMP,
                last_notification_at TIMESTAMP,
                last_notification_status VARCHAR(32),
                last_notification_message VARCHAR(1024)
            )
            """);
        alertJdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS alert_trigger (
                id VARCHAR(64) PRIMARY KEY,
                rule_id VARCHAR(64) NOT NULL,
                rule_name VARCHAR(128) NOT NULL,
                metric_type VARCHAR(64) NOT NULL,
                observed_value DOUBLE,
                threshold DOUBLE,
                comparator VARCHAR(8),
                triggered_at TIMESTAMP NOT NULL,
                notification_channel VARCHAR(32),
                notification_target VARCHAR(512),
                notification_status VARCHAR(32),
                notification_message VARCHAR(1024),
                details VARCHAR(4000)
            )
            """);
    }

    public AlertRuleResponse createAlertRule(AlertRuleRequest request) {
        validateAlertRule(request);
        LocalDateTime now = LocalDateTime.now();
        AlertRuleResponse response = toAlertRuleResponse(UUID.randomUUID().toString(), request, now, now);
        alertJdbcTemplate.update("""
                INSERT INTO alert_rule (
                    id, rule_name, metric_type, service, api_endpoint, downstream_service, error_type, percentile,
                    client_ip, user_id, timeout_threshold_ms, window_size, comparator, threshold, consecutive_times,
                    notification_channel, notification_target, enabled, created_time, updated_time,
                    current_consecutive_count, trigger_count
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                """,
            response.getId(),
            response.getRuleName(),
            response.getMetricType(),
            response.getService(),
            response.getApiEndpoint(),
            response.getDownstreamService(),
            response.getErrorType(),
            response.getPercentile(),
            response.getClientIp(),
            response.getUserId(),
            response.getTimeoutThresholdMs(),
            response.getWindowSize(),
            response.getComparator(),
            response.getThreshold(),
            response.getConsecutiveTimes(),
            response.getNotificationChannel(),
            response.getNotificationTarget(),
            response.getEnabled(),
            response.getCreatedTime(),
            response.getUpdatedTime()
        );
        return getAlertRule(response.getId());
    }

    public AlertRuleResponse updateAlertRule(String ruleId, AlertRuleRequest request) {
        validateAlertRule(request);
        AlertRuleResponse existing = getAlertRule(ruleId);
        AlertRuleResponse response = toAlertRuleResponse(ruleId, request, existing.getCreatedTime(), LocalDateTime.now());
        int updated = alertJdbcTemplate.update("""
                UPDATE alert_rule
                SET rule_name = ?, metric_type = ?, service = ?, api_endpoint = ?, downstream_service = ?,
                    error_type = ?, percentile = ?, client_ip = ?, user_id = ?, timeout_threshold_ms = ?,
                    window_size = ?, comparator = ?, threshold = ?, consecutive_times = ?,
                    notification_channel = ?, notification_target = ?, enabled = ?, updated_time = ?
                WHERE id = ?
                """,
            response.getRuleName(),
            response.getMetricType(),
            response.getService(),
            response.getApiEndpoint(),
            response.getDownstreamService(),
            response.getErrorType(),
            response.getPercentile(),
            response.getClientIp(),
            response.getUserId(),
            response.getTimeoutThresholdMs(),
            response.getWindowSize(),
            response.getComparator(),
            response.getThreshold(),
            response.getConsecutiveTimes(),
            response.getNotificationChannel(),
            response.getNotificationTarget(),
            response.getEnabled(),
            response.getUpdatedTime(),
            ruleId
        );
        if (updated == 0) {
            throw new BusinessException(404, "alert rule not found");
        }
        return getAlertRule(ruleId);
    }

    public PageResponse<AlertRuleResponse> listAlertRules(Integer page, Integer pageSize, String metricType, String service, Boolean enabled) {
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        List<Object> params = new ArrayList<>();
        String whereSql = buildRuleWhere(metricType, service, enabled, params);
        Long total = alertJdbcTemplate.queryForObject("SELECT COUNT(*) FROM alert_rule WHERE " + whereSql, Long.class, params.toArray());
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(normalizedPageSize);
        listParams.add((normalizedPage - 1) * normalizedPageSize);
        List<AlertRuleResponse> rows = alertJdbcTemplate.query(
            "SELECT * FROM alert_rule WHERE " + whereSql + " ORDER BY created_time DESC LIMIT ? OFFSET ?",
            this::mapAlertRule,
            listParams.toArray()
        );
        return PageResponse.<AlertRuleResponse>builder()
            .page(normalizedPage)
            .pageSize(normalizedPageSize)
            .total(total == null ? 0 : total)
            .list(rows)
            .build();
    }

    public AlertRuleResponse getAlertRule(String ruleId) {
        List<AlertRuleResponse> rows = alertJdbcTemplate.query(
            "SELECT * FROM alert_rule WHERE id = ?",
            this::mapAlertRule,
            ruleId
        );
        if (rows.isEmpty()) {
            throw new BusinessException(404, "alert rule not found");
        }
        return rows.get(0);
    }

    public void deleteAlertRule(String ruleId) {
        int deleted = alertJdbcTemplate.update("DELETE FROM alert_rule WHERE id = ?", ruleId);
        if (deleted == 0) {
            throw new BusinessException(404, "alert rule not found");
        }
    }

    public PageResponse<AlertTriggerRecord> listTriggerRecords(Integer page, Integer pageSize, String ruleId, String metricType) {
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        List<String> filters = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        filters.add("1 = 1");
        if (StringUtils.hasText(ruleId)) {
            filters.add("rule_id = ?");
            params.add(ruleId);
        }
        if (StringUtils.hasText(metricType)) {
            filters.add("LOWER(metric_type) = ?");
            params.add(metricType.toLowerCase(Locale.ROOT));
        }
        String whereSql = String.join(" AND ", filters);
        Long total = alertJdbcTemplate.queryForObject("SELECT COUNT(*) FROM alert_trigger WHERE " + whereSql, Long.class, params.toArray());
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(normalizedPageSize);
        listParams.add((normalizedPage - 1) * normalizedPageSize);
        List<AlertTriggerRecord> rows = alertJdbcTemplate.query(
            "SELECT * FROM alert_trigger WHERE " + whereSql + " ORDER BY triggered_at DESC LIMIT ? OFFSET ?",
            this::mapAlertTrigger,
            listParams.toArray()
        );
        return PageResponse.<AlertTriggerRecord>builder()
            .page(normalizedPage)
            .pageSize(normalizedPageSize)
            .total(total == null ? 0 : total)
            .list(rows)
            .build();
    }

    @Scheduled(
        initialDelayString = "${log-service.alert.evaluation-delay-ms:60000}",
        fixedDelayString = "${log-service.alert.evaluation-delay-ms:60000}"
    )
    public void evaluateAlertRules() {
        if (!properties.getAlert().isEnabled()) {
            return;
        }
        List<AlertRuleResponse> rules = alertJdbcTemplate.query(
            "SELECT * FROM alert_rule WHERE enabled = TRUE ORDER BY created_time ASC",
            this::mapAlertRule
        );
        for (AlertRuleResponse rule : rules) {
            try {
                evaluateRule(rule);
            } catch (RuntimeException exception) {
                log.warn("alert rule evaluation failed: {}", rule.getId(), exception);
                markEvaluationFailed(rule.getId(), exception);
            }
        }
    }

    private void evaluateRule(AlertRuleResponse rule) {
        LocalDateTime now = LocalDateTime.now();
        EvaluatedMetric evaluatedMetric = readMetricValue(rule);
        boolean matched = compare(evaluatedMetric.value(), rule.getComparator(), rule.getThreshold());
        int currentCount = matched ? nullSafe(rule.getCurrentConsecutiveCount()) + 1 : 0;
        updateEvaluation(rule.getId(), now, evaluatedMetric.value(), currentCount);
        if (!matched || currentCount < rule.getConsecutiveTimes()) {
            return;
        }
        if (isNotificationCoolingDown(rule, now)) {
            updateNotificationState(rule.getId(), "cooldown", "notification cooldown active", now, false);
            return;
        }
        NotificationResult notification = sendNotification(rule, evaluatedMetric);
        recordTrigger(rule, evaluatedMetric, notification, now);
        updateNotificationState(rule.getId(), notification.status(), notification.message(), now, true);
    }

    private EvaluatedMetric readMetricValue(AlertRuleResponse rule) {
        MetricQueryRequest request = new MetricQueryRequest();
        long windowSeconds = parseWindowSeconds(rule.getWindowSize());
        Instant end = Instant.now();
        request.setStartTime(end.minusSeconds(windowSeconds).toString());
        request.setEndTime(end.toString());
        request.setWindowSize(rule.getWindowSize());
        request.setService(rule.getService());
        request.setApiEndpoint(rule.getApiEndpoint());
        request.setDownstreamService(rule.getDownstreamService());
        request.setErrorType(rule.getErrorType());
        request.setPercentile(rule.getPercentile());
        request.setClientIp(rule.getClientIp());
        request.setUserId(rule.getUserId());
        request.setTimeoutThresholdMs(rule.getTimeoutThresholdMs());

        MetricSeriesResponse series = switch (rule.getMetricType().toLowerCase(Locale.ROOT)) {
            case "request_rate" -> logAnalysisService.requestRate(request);
            case "request_timeout_rate" -> logAnalysisService.timeoutRate(request);
            case "request_error_rate" -> logAnalysisService.errorRate(request);
            case "request_latency" -> logAnalysisService.latency(request);
            case "downstream_error_rate" -> logAnalysisService.downstreamErrorRate(request);
            case "downstream_latency" -> logAnalysisService.downstreamLatency(request);
            case "ip_request_rate" -> logAnalysisService.ipRequestRate(request);
            case "user_request_rate" -> logAnalysisService.userRequestRate(request);
            default -> throw new BusinessException(400, "metric_type is not supported");
        };
        List<MetricPoint> points = Optional.ofNullable(series.getPoints()).orElse(List.of()).stream()
            .filter(point -> point.getValue() != null)
            .toList();
        if (points.isEmpty()) {
            return new EvaluatedMetric(0D, null, "no metric points");
        }
        Comparator<MetricPoint> comparator = Comparator.comparing(MetricPoint::getValue);
        MetricPoint selected = isLessComparator(rule.getComparator())
            ? points.stream().min(comparator).orElse(points.get(0))
            : points.stream().max(comparator).orElse(points.get(0));
        return new EvaluatedMetric(selected.getValue(), selected, serializeDetails(selected));
    }

    private NotificationResult sendNotification(AlertRuleResponse rule, EvaluatedMetric metric) {
        String channel = StringUtils.hasText(rule.getNotificationChannel())
            ? rule.getNotificationChannel()
            : properties.getAlert().getDefaultNotificationChannel();
        if (!"in_app".equalsIgnoreCase(channel)) {
            return new NotificationResult("skipped", "only in_app notification is configured");
        }
        List<String> userIds = parseTargets(rule.getNotificationTarget());
        if (userIds.isEmpty()) {
            return new NotificationResult("skipped", "notification_target is empty");
        }
        String baseUrl = normalizeBaseUrl(properties.getAlert().getNotificationServiceUrl());
        if (!StringUtils.hasText(baseUrl)) {
            return new NotificationResult("skipped", "notification_service_url is empty");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userIds", userIds);
        payload.put("title", "Access log alert: " + rule.getRuleName());
        payload.put("content", buildNotificationContent(rule, metric));
        payload.put("category", "log_alert");
        payload.put("creatorId", "log-service");
        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/internal/site-messages/send"))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new NotificationResult("sent", "site message sent");
            }
            return new NotificationResult("failed", abbreviate("notification status " + response.statusCode() + ": " + response.body(), 900));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new NotificationResult("failed", "notification interrupted");
        } catch (IOException | RuntimeException exception) {
            return new NotificationResult("failed", abbreviate(exception.getMessage(), 900));
        }
    }

    private void recordTrigger(AlertRuleResponse rule, EvaluatedMetric metric, NotificationResult notification, LocalDateTime triggeredAt) {
        alertJdbcTemplate.update("""
                INSERT INTO alert_trigger (
                    id, rule_id, rule_name, metric_type, observed_value, threshold, comparator, triggered_at,
                    notification_channel, notification_target, notification_status, notification_message, details
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            UUID.randomUUID().toString(),
            rule.getId(),
            rule.getRuleName(),
            rule.getMetricType(),
            metric.value(),
            rule.getThreshold(),
            rule.getComparator(),
            triggeredAt,
            rule.getNotificationChannel(),
            rule.getNotificationTarget(),
            notification.status(),
            abbreviate(notification.message(), 1000),
            abbreviate(metric.details(), 4000)
        );
    }

    private void updateEvaluation(String ruleId, LocalDateTime evaluatedAt, Double value, int currentCount) {
        alertJdbcTemplate.update("""
                UPDATE alert_rule
                SET last_evaluated_at = ?, last_metric_value = ?, current_consecutive_count = ?
                WHERE id = ?
                """,
            evaluatedAt,
            value,
            currentCount,
            ruleId
        );
    }

    private void updateNotificationState(String ruleId, String status, String message, LocalDateTime now, boolean incrementTrigger) {
        if (incrementTrigger) {
            alertJdbcTemplate.update("""
                    UPDATE alert_rule
                    SET trigger_count = COALESCE(trigger_count, 0) + 1,
                        last_triggered_at = ?,
                        last_notification_at = ?,
                        last_notification_status = ?,
                        last_notification_message = ?
                    WHERE id = ?
                    """,
                now,
                now,
                status,
                abbreviate(message, 1000),
                ruleId
            );
            return;
        }
        alertJdbcTemplate.update("""
                UPDATE alert_rule
                SET last_notification_status = ?, last_notification_message = ?
                WHERE id = ?
                """,
            status,
            abbreviate(message, 1000),
            ruleId
        );
    }

    private void markEvaluationFailed(String ruleId, RuntimeException exception) {
        alertJdbcTemplate.update("""
                UPDATE alert_rule
                SET last_evaluated_at = ?, last_notification_status = ?, last_notification_message = ?
                WHERE id = ?
                """,
            LocalDateTime.now(),
            "evaluation_failed",
            abbreviate(exception.getMessage(), 1000),
            ruleId
        );
    }

    private boolean isNotificationCoolingDown(AlertRuleResponse rule, LocalDateTime now) {
        LocalDateTime lastNotificationAt = rule.getLastNotificationAt();
        if (lastNotificationAt == null) {
            return false;
        }
        long cooldown = properties.getAlert().getNotificationCooldownMinutes();
        return cooldown > 0 && Duration.between(lastNotificationAt, now).toMinutes() < cooldown;
    }

    private String buildRuleWhere(String metricType, String service, Boolean enabled, List<Object> params) {
        List<String> filters = new ArrayList<>();
        filters.add("1 = 1");
        if (StringUtils.hasText(metricType)) {
            filters.add("LOWER(metric_type) = ?");
            params.add(metricType.toLowerCase(Locale.ROOT));
        }
        if (StringUtils.hasText(service)) {
            filters.add("service = ?");
            params.add(service);
        }
        if (enabled != null) {
            filters.add("enabled = ?");
            params.add(enabled);
        }
        return String.join(" AND ", filters);
    }

    private AlertRuleResponse mapAlertRule(ResultSet rs, int rowNum) throws SQLException {
        return AlertRuleResponse.builder()
            .id(rs.getString("id"))
            .ruleName(rs.getString("rule_name"))
            .metricType(rs.getString("metric_type"))
            .service(rs.getString("service"))
            .apiEndpoint(rs.getString("api_endpoint"))
            .downstreamService(rs.getString("downstream_service"))
            .errorType(rs.getString("error_type"))
            .percentile(rs.getString("percentile"))
            .clientIp(rs.getString("client_ip"))
            .userId(rs.getString("user_id"))
            .timeoutThresholdMs(readLong(rs, "timeout_threshold_ms"))
            .windowSize(rs.getString("window_size"))
            .comparator(rs.getString("comparator"))
            .threshold(rs.getDouble("threshold"))
            .consecutiveTimes(rs.getInt("consecutive_times"))
            .notificationChannel(rs.getString("notification_channel"))
            .notificationTarget(rs.getString("notification_target"))
            .enabled(rs.getBoolean("enabled"))
            .createdTime(readTime(rs, "created_time"))
            .updatedTime(readTime(rs, "updated_time"))
            .lastEvaluatedAt(readTime(rs, "last_evaluated_at"))
            .lastValue(readDouble(rs, "last_metric_value"))
            .currentConsecutiveCount(rs.getInt("current_consecutive_count"))
            .triggerCount(rs.getInt("trigger_count"))
            .lastTriggeredAt(readTime(rs, "last_triggered_at"))
            .lastNotificationAt(readTime(rs, "last_notification_at"))
            .lastNotificationStatus(rs.getString("last_notification_status"))
            .lastNotificationMessage(rs.getString("last_notification_message"))
            .build();
    }

    private AlertTriggerRecord mapAlertTrigger(ResultSet rs, int rowNum) throws SQLException {
        return AlertTriggerRecord.builder()
            .id(rs.getString("id"))
            .ruleId(rs.getString("rule_id"))
            .ruleName(rs.getString("rule_name"))
            .metricType(rs.getString("metric_type"))
            .observedValue(readDouble(rs, "observed_value"))
            .threshold(readDouble(rs, "threshold"))
            .comparator(rs.getString("comparator"))
            .triggeredAt(readTime(rs, "triggered_at"))
            .notificationChannel(rs.getString("notification_channel"))
            .notificationTarget(rs.getString("notification_target"))
            .notificationStatus(rs.getString("notification_status"))
            .notificationMessage(rs.getString("notification_message"))
            .details(rs.getString("details"))
            .build();
    }

    private AlertRuleResponse toAlertRuleResponse(String id, AlertRuleRequest request, LocalDateTime createdTime, LocalDateTime updatedTime) {
        return AlertRuleResponse.builder()
            .id(id)
            .ruleName(request.getRuleName())
            .metricType(request.getMetricType().toLowerCase(Locale.ROOT))
            .service(blankToNull(request.getService()))
            .apiEndpoint(blankToNull(request.getApiEndpoint()))
            .downstreamService(blankToNull(request.getDownstreamService()))
            .errorType(blankToNull(request.getErrorType()))
            .percentile(blankToNull(request.getPercentile()))
            .clientIp(blankToNull(request.getClientIp()))
            .userId(blankToNull(request.getUserId()))
            .timeoutThresholdMs(request.getTimeoutThresholdMs())
            .windowSize(request.getWindowSize())
            .comparator(request.getComparator())
            .threshold(request.getThreshold())
            .consecutiveTimes(request.getConsecutiveTimes() == null ? 1 : request.getConsecutiveTimes())
            .notificationChannel(StringUtils.hasText(request.getNotificationChannel())
                ? request.getNotificationChannel()
                : properties.getAlert().getDefaultNotificationChannel())
            .notificationTarget(blankToNull(request.getNotificationTarget()))
            .enabled(request.getEnabled() == null || request.getEnabled())
            .createdTime(createdTime)
            .updatedTime(updatedTime)
            .currentConsecutiveCount(0)
            .triggerCount(0)
            .build();
    }

    private void validateAlertRule(AlertRuleRequest request) {
        String metricType = request.getMetricType();
        if (SUPPORTED_METRICS.stream().noneMatch(metric -> metric.equalsIgnoreCase(metricType))) {
            throw new BusinessException(400, "metric_type is not supported");
        }
        parseWindowSeconds(request.getWindowSize());
        String comparator = request.getComparator();
        if (List.of(">", ">=", "<", "<=", "=", "==").stream().noneMatch(item -> item.equals(comparator))) {
            throw new BusinessException(400, "comparator only supports >, >=, <, <=, =, ==");
        }
        if (request.getConsecutiveTimes() != null && request.getConsecutiveTimes() < 1) {
            throw new BusinessException(400, "consecutive_times must be greater than 0");
        }
    }

    private boolean compare(double value, String comparator, double threshold) {
        return switch (comparator) {
            case ">" -> value > threshold;
            case ">=" -> value >= threshold;
            case "<" -> value < threshold;
            case "<=" -> value <= threshold;
            case "=", "==" -> Double.compare(value, threshold) == 0;
            default -> throw new BusinessException(400, "comparator only supports >, >=, <, <=, =, ==");
        };
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

    private String serializeDetails(MetricPoint point) {
        if (point == null) {
            return null;
        }
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("bucket_start", point.getBucketStart());
        details.put("bucket_end", point.getBucketEnd());
        details.put("group_key", point.getGroupKey());
        details.put("service", point.getService());
        details.put("api_endpoint", point.getApiEndpoint());
        details.put("downstream_service", point.getDownstreamService());
        details.put("client_ip", point.getClientIp());
        details.put("user_id", point.getUserId());
        details.put("total", point.getTotal());
        details.put("matched", point.getMatched());
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            return details.toString();
        }
    }

    private String buildNotificationContent(AlertRuleResponse rule, EvaluatedMetric metric) {
        return "Rule " + rule.getRuleName()
            + " matched. metric=" + rule.getMetricType()
            + ", value=" + metric.value()
            + ", comparator=" + rule.getComparator()
            + ", threshold=" + rule.getThreshold()
            + ", service=" + Objects.toString(rule.getService(), "-");
    }

    private List<String> parseTargets(String target) {
        if (!StringUtils.hasText(target)) {
            return List.of();
        }
        return Arrays.stream(target.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
    }

    private String normalizeBaseUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return null;
        }
        return rawUrl.endsWith("/") ? rawUrl.substring(0, rawUrl.length() - 1) : rawUrl;
    }

    private boolean isLessComparator(String comparator) {
        return "<".equals(comparator) || "<=".equals(comparator);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        int value = pageSize == null || pageSize < 1 ? properties.getDefaultPageSize() : pageSize;
        return Math.min(value, properties.getMaxPageSize());
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LocalDateTime readTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private Double readDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private Long readLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record EvaluatedMetric(Double value, MetricPoint point, String details) {
    }

    private record NotificationResult(String status, String message) {
    }
}
