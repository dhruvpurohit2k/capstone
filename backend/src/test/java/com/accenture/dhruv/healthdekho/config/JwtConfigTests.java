package com.accenture.dhruv.healthdekho.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtConfigTests {

    @Test
    void signsWithTheLiteralUtf8Secret() throws Exception {
        String secret = "x!é";
        var jwt = config(secret);
        String token = jwt.createToken(42);
        int signatureStart = token.lastIndexOf('.');

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signature = mac.doFinal(token.substring(0, signatureStart).getBytes(StandardCharsets.UTF_8));

        assertThat(token.substring(signatureStart + 1))
                .isEqualTo(Base64.getUrlEncoder().withoutPadding().encodeToString(signature));
        assertThat(jwt.getCustomerId(token)).contains(42L);
        assertThat(jwt.getRefreshCustomerId(jwt.refreshCookie(42).getValue())).contains(42L);
    }

    @Test
    void rejectsABlankSecret() {
        assertThatThrownBy(() -> config(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET must not be blank");
    }

    private JwtConfig config(String secret) {
        return new JwtConfig(new ObjectMapper(), secret, Duration.ofMinutes(15), Duration.ofDays(7), true);
    }
}
