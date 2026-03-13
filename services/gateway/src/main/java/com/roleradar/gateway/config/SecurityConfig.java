package com.roleradar.gateway.config;

import com.roleradar.gateway.security.AccessTokenCookieServerAuthenticationConverter;
import com.roleradar.gateway.security.JwtAudienceValidator;
import com.roleradar.gateway.security.JwtAuthenticationConverter;
import com.roleradar.gateway.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                     ReactiveJwtDecoder jwtDecoder,
                                                     JwtProperties jwtProperties) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // TODO complete csrf defence
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenConverter(
                                new AccessTokenCookieServerAuthenticationConverter(
                                        jwtProperties.accessTokenCookieName()
                                )
                        )
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder.withJwkSetUri(jwtProperties.jwkSetUri()).build();

        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer());

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtAudienceValidator(jwtProperties.audience());

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        decoder.setJwtValidator(validator);
        return decoder;
    }
}