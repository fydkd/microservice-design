package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricPoint {

    @JsonProperty("bucket_start")
    private String bucketStart;

    @JsonProperty("bucket_end")
    private String bucketEnd;

    private String service;

    @JsonProperty("api_endpoint")
    private String apiEndpoint;

    @JsonProperty("downstream_service")
    private String downstreamService;

    @JsonProperty("client_ip")
    private String clientIp;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("group_key")
    private String groupKey;

    private Double value;

    private Long total;

    private Long matched;
}
