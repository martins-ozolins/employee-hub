package com.employeehub.employeehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        int presignExpireMinutes
) {}