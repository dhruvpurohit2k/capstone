package com.accenture.dhruv.healthdekho.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
public class JwtConfig {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER = ENCODER.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final String ISSUER = "healthdekho";

    private final ObjectMapper mapper;
    private final SecretKeySpec key;
    private final Duration lifetime;

    public JwtConfig(ObjectMapper mapper, @Value("${app.jwt.secret}") String secret,
                     @Value("${app.jwt.access-token-ttl:PT15M}") Duration lifetime) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32 || lifetime.toSeconds() < 1) {
            throw new IllegalArgumentException("JWT requires a Base64 key of at least 32 bytes and a positive lifetime");
        }
        this.mapper = mapper;
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.lifetime = lifetime;
    }

    public String createToken(long customerId) {
        var now = Instant.now();
        var claims = new Claims(ISSUER, Long.toString(customerId), now.getEpochSecond(),
                now.plus(lifetime).getEpochSecond());
        try {
            String input = HEADER + "." + ENCODER.encodeToString(mapper.writeValueAsBytes(claims));
            return input + "." + sign(input);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not encode JWT", e);
        }
    }

    public Optional<Long> getCustomerId(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || !HEADER.equals(parts[0])) {
            return Optional.empty();
        }
        try {
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            Claims claims = mapper.readValue(DECODER.decode(parts[1]), Claims.class);
            long now = Instant.now().getEpochSecond();
            if (claims == null || !ISSUER.equals(claims.iss()) 
                    || claims.iat() <= 0 || claims.iat() > now || claims.exp() <= now) {
                return Optional.empty();
            }
            long customerId = Long.parseLong(claims.sub());
            return customerId > 0 ? Optional.of(customerId) : Optional.empty();
        } catch (java.io.IOException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return ENCODER.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign JWT", e);
        }
    }

    private record Claims(String iss, String sub, long iat, long exp) {
    }
}
