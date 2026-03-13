package com.roleradar.auth.security;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class PemKeyLoader {

    private final ResourceLoader resourceLoader;

    public PemKeyLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public PrivateKey loadPrivateKey(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String normalized = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] der = Base64.getDecoder().decode(normalized);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(der);

            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA private key from " + location, e);
        }
    }
}
