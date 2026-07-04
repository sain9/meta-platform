package com.hussain.data_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "data.api")
@Data
public class DataApiProperties {
    private int defaultLimit = 100;
    private int maxLimit = 1000;
    private boolean enableAudit = true;
    private boolean enableQueryLogging = true;
    private boolean enableCache = false;
    private int cacheTimeout = 300;
}