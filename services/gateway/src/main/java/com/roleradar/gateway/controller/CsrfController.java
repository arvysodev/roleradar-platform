package com.roleradar.gateway.controller;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class CsrfController {

    @GetMapping("/csrf")
    public Mono<Map<String, String>> csrf(ServerWebExchange exchange) {
        Mono<CsrfToken> csrfTokenMono =
                exchange.getAttribute(CsrfToken.class.getName());

        if (csrfTokenMono == null) {
            return Mono.error(new IllegalStateException("CSRF token was not found in exchange attributes."));
        }

        return csrfTokenMono.map(token -> Map.of("token", token.getToken()));
    }
}
