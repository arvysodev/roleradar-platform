package com.roleradar.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.cookies")
@Getter
@Setter
public class CookieProperties {

    private boolean secure = false;
    private String sameSite = "Lax";
}