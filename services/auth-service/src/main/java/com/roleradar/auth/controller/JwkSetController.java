package com.roleradar.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.roleradar.auth.security.JwtKeyPairProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class JwkSetController {

    private final JwtKeyPairProvider jwtKeyPairProvider;

    public JwkSetController(JwtKeyPairProvider jwtKeyPairProvider) {
        this.jwtKeyPairProvider = jwtKeyPairProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwkSet() {
        RSAPublicKey publicKey = (RSAPublicKey) jwtKeyPairProvider.getPublicKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(jwtKeyPairProvider.getKeyId())
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .build();

        return new JWKSet(rsaKey).toJSONObject();
    }
}