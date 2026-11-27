package com.accenture.dhruv.healthdekho.config;

import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.accenture.dhruv.healthdekho.exception.UnauthenticatedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtConfig jwt, ObjectMapper mapper,
                                                   @Value("${app.jwt.secure-cookies}") boolean secureCookies)
            throws Exception {
        var csrfTokens = new CookieCsrfTokenRepository();
        csrfTokens.setCookieCustomizer(cookie -> cookie.httpOnly(true).secure(secureCookies).sameSite("Strict"));

        return http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.accessDeniedHandler((request, response, cause) -> {
                    boolean csrfError = cause instanceof CsrfException;
                    var error = new ApiException(HttpStatus.FORBIDDEN, csrfError ? "csrf" : "Authorization",
                            csrfError ? "A valid CSRF token is required" : "Access denied");
                    response.setStatus(error.status().value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    mapper.writeValue(response.getOutputStream(), error.response());
                }).authenticationEntryPoint((request, response, cause) -> {
                    var error = new UnauthenticatedException(HttpHeaders.AUTHORIZATION,
                            "A valid Bearer token is required");
                    response.setStatus(error.status().value());
                    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    mapper.writeValue(response.getOutputStream(), error.response());
                }))
                .addFilterBefore((request, response, chain) -> {
                    authenticate((HttpServletRequest) request, jwt);
                    chain.doFilter(request, response);
                }, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void authenticate(HttpServletRequest request, JwtConfig jwt) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return;
        }
        jwt.getCustomerId(authorization.substring(7)).ifPresent(id -> {
            var authentication = UsernamePasswordAuthenticationToken.authenticated(id.toString(), null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    }
}
