package com.roleradar.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                .route("auth-service-openapi", r -> r
                        .path("/aggregate/auth-service/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://localhost:8081"))
                .route("vacancy-service", r -> r
                        .path("/api/v1/vacancies/**")
                        .uri("http://localhost:8082"))
                .build();
    }
}