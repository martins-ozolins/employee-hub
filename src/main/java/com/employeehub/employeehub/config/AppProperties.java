package com.employeehub.employeehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String baseUrl,
        Document document
) {
    public record Document(
            long maxSizeBytes,
            List<String> allowedContentTypes
    ) {}
}