package com.roleradar.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

@Component
public class GlobalErrorAttributes {

    public Map<String, Object> toErrorAttributes(Throwable error, ServerWebExchange exchange) {
        ProblemDetail problemDetail = toProblemDetail(error, exchange);

        return Map.of(
                "type", problemDetail.getType(),
                "title", problemDetail.getTitle(),
                "status", problemDetail.getStatus(),
                "detail", problemDetail.getDetail(),
                "instance", problemDetail.getInstance()
        );
    }

    private ProblemDetail toProblemDetail(Throwable error, ServerWebExchange exchange) {
        if (error instanceof BadRequestException ex) {
            return ProblemDetailsSupport.problem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), exchange);
        }

        if (error instanceof NotFoundException ex) {
            return ProblemDetailsSupport.problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), exchange);
        }

        if (error instanceof ConflictException ex) {
            return ProblemDetailsSupport.problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), exchange);
        }

        if (error instanceof UnauthorizedException ex) {
            return ProblemDetailsSupport.problem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), exchange);
        }

        if (error instanceof ForbiddenException ex) {
            return ProblemDetailsSupport.problem(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), exchange);
        }

        if (error instanceof WebExchangeBindException) {
            return ProblemDetailsSupport.problem(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "Validation failed.",
                    exchange
            );
        }

        return ProblemDetailsSupport.problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected error occurred.",
                exchange
        );
    }
}
