package com.roleradar.auth.security;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

@Component
@Getter
public class JwtKeyPairProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final JwtProperties jwtProperties;

    public JwtKeyPairProvider(JwtProperties jwtProperties, PemKeyLoader pemKeyLoader) {
        try {
            this.jwtProperties = jwtProperties;
            this.privateKey = pemKeyLoader.loadPrivateKey(jwtProperties.privateKeyLocation());

            if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
                throw new IllegalStateException("Configured private key is not an RSA private CRT key.");
            }

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPublicExponent()
            );

            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JWT key pair.", e);
        }
    }

    public String getKeyId() {
        return jwtProperties.keyId();
    }
}