package com.roleradar.ingestion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient remotiveRestClient(
            @Value("${roleradar.ingestion.remotive.base-url}") String baseUrl,
            @Value("${roleradar.ingestion.http.connect-timeout-seconds}") long connectionTimeoutSeconds,
            @Value("${roleradar.ingestion.http.read-timeout-seconds}") long readTimeoutSeconds
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(connectionTimeoutSeconds, readTimeoutSeconds))
                .build();
    }

    @Bean
    public RestClient arbeitnowRestClient(
            @Value("${roleradar.ingestion.arbeitnow.base-url}") String baseUrl,
            @Value("${roleradar.ingestion.http.connect-timeout-seconds}") long connectionTimeoutSeconds,
            @Value("${roleradar.ingestion.http.read-timeout-seconds}") long readTimeoutSeconds
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(connectionTimeoutSeconds, readTimeoutSeconds))
                .build();
    }

    @Bean
    public RestClient adzunaRestClient(
            @Value("${roleradar.ingestion.adzuna.base-url}") String baseUrl,
            @Value("${roleradar.ingestion.http.connect-timeout-seconds}") long connectionTimeoutSeconds,
            @Value("${roleradar.ingestion.http.read-timeout-seconds}") long readTimeoutSeconds
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(connectionTimeoutSeconds, readTimeoutSeconds))
                .build();
    }

    private JdkClientHttpRequestFactory requestFactory(
            long connectionTimeoutSeconds,
            long readTimeoutSeconds
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        return requestFactory;
    }
}
