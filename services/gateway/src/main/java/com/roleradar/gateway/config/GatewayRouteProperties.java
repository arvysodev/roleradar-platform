package com.roleradar.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.routes")
public record GatewayRouteProperties(
        String authServiceUri,
        String vacancyServiceUri
) {
}
