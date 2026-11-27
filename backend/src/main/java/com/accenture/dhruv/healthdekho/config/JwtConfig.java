package com.accenture.dhruv.healthdekho.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;

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

    public static final String REFRESH_COOKIE = "healthdekho_refresh";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER = ENCODER.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final String ISSUER = "healthdekho";

    private final ObjectMapper mapper;
    private final SecretKeySpec key;
    private final Duration accessLifetime;
    private final Duration refreshLifetime;
    private final boolean secureCookies;

    public JwtConfig(ObjectMapper mapper, @Value("${app.jwt.secret}") String secret,
                     @Value("${app.jwt.access-token-ttl}") Duration accessLifetime,
                     @Value("${app.jwt.refresh-token-ttl}") Duration refreshLifetime,
                     @Value("${app.jwt.secure-cookies}") boolean secureCookies) {
        if (secret.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET must not be blank");
        }
        if (accessLifetime.toSeconds() < 1 || refreshLifetime.toSeconds() < 1) {
            throw new IllegalArgumentException("JWT token lifetimes must be at least one second");
        }
        this.mapper = mapper;
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.accessLifetime = accessLifetime;
        this.refreshLifetime = refreshLifetime;
        this.secureCookies = secureCookies;
    }

    public String createToken(long customerId) {
        return createToken(customerId, "access", accessLifetime);
    }

    public ResponseCookie refreshCookie(long customerId) {
        return refreshCookie(createToken(customerId, "refresh", refreshLifetime), refreshLifetime);
    }

    public ResponseCookie clearRefreshCookie() {
        return refreshCookie("", Duration.ZERO);
    }

    private ResponseCookie refreshCookie(String token, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true).secure(secureCookies).sameSite("Strict").path("/api/auth")
                .maxAge(maxAge).build();
    }

    private String createToken(long customerId, String purpose, Duration lifetime) {
        var now = Instant.now();
        var claims = new Claims(ISSUER, Long.toString(customerId), now.getEpochSecond(),
                now.plus(lifetime).getEpochSecond(), purpose);
        try {
            String input = HEADER + "." + ENCODER.encodeToString(mapper.writeValueAsBytes(claims));
            return input + "." + sign(input);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not encode JWT", e);
        }
    }

    public Optional<Long> getCustomerId(String token) {
        return getCustomerId(token, "access");
    }

    public Optional<Long> getRefreshCustomerId(String token) {
        return getCustomerId(token, "refresh");
    }

    private Optional<Long> getCustomerId(String token, String purpose) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
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
            if (claims == null || !ISSUER.equals(claims.iss()) || !purpose.equals(claims.purpose())
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

    private record Claims(String iss, String sub, long iat, long exp, String purpose) {
    }
}
