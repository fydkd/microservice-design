package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSeriesResponse {

    @JsonProperty("metric_name")
    private String metricName;

    private String unit;

    @JsonProperty("window_size")
    private String windowSize;

    private List<MetricPoint> points;
}
