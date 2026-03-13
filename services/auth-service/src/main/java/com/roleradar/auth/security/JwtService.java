package com.roleradar.auth.security;

import com.roleradar.auth.domain.User;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final JwtProperties jwtProperties;
    private final JwtKeyPairProvider jwtKeyPairProvider;

    public JwtService(JwtProperties jwtProperties, JwtKeyPairProvider jwtKeyPairProvider) {
        this.jwtProperties = jwtProperties;
        this.jwtKeyPairProvider = jwtKeyPairProvider;
        this.privateKey = jwtKeyPairProvider.getPrivateKey();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.accessTokenTtlSeconds());

        return Jwts.builder()
                .header()
                .keyId(jwtKeyPairProvider.getKeyId())
                .and()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .audience().add(jwtProperties.audience()).and()
                .claim("email", user.getEmail())
                .claim("preferred_username", user.getUsername())
                .claim("roles", List.of(user.getRole().name()))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return jwtProperties.accessTokenTtlSeconds();
    }

    public long getRefreshTokenTtlSeconds() {
        return jwtProperties.refreshTokenTtlSeconds();
    }
}