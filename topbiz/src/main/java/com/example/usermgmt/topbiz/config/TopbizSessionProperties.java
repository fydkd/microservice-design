package com.example.usermgmt.topbiz.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "topbiz.session")
public class TopbizSessionProperties {

    private Duration ttl = Duration.ofHours(12);
    private boolean singleLoginEnabled = false;
}
