package com.roleradar.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

public final class ProblemDetailsSupport {

    private ProblemDetailsSupport() {
    }

    public static ProblemDetail problem(HttpStatus status, String title, String detail, ServerWebExchange exchange) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://roleradar.app/problems/" + toSlug(title)));
        pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
        return pd;
    }

    private static String toSlug(String title) {
        return title.toLowerCase().replace(' ', '-');
    }
}
