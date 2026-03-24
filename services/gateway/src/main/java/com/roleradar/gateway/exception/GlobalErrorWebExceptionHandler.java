package com.roleradar.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final GlobalErrorAttributes errorAttributes;
    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(GlobalErrorAttributes errorAttributes,
                                          ObjectMapper objectMapper) {
        this.errorAttributes = errorAttributes;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        if (!(ex instanceof BadRequestException)
                && !(ex instanceof NotFoundException)
                && !(ex instanceof ConflictException)
                && !(ex instanceof UnauthorizedException)
                && !(ex instanceof ForbiddenException)) {
            log.error("Unhandled exception occurred", ex);
        }

        Map<String, Object> errorBody = errorAttributes.toErrorAttributes(ex, exchange);

        Object status = errorBody.get("status");
        int statusCode = status instanceof Integer value ? value : 500;

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(statusCode));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            var buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception writeEx) {
            byte[] fallback = """
                    {"type":"https://roleradar.app/problems/internal-server-error","title":"Internal Server Error","status":500,"detail":"Unexpected error occurred."}
                    """.getBytes(StandardCharsets.UTF_8);
            var buffer = exchange.getResponse().bufferFactory().wrap(fallback);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}
