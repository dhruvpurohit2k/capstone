package com.accenture.dhruv.healthdekho.auth.controller;

import com.accenture.dhruv.healthdekho.auth.dto.CsrfResponse;
import com.accenture.dhruv.healthdekho.auth.dto.LoginRequest;
import com.accenture.dhruv.healthdekho.auth.dto.LoginResponse;
import com.accenture.dhruv.healthdekho.auth.dto.LogoutResponse;
import com.accenture.dhruv.healthdekho.auth.dto.SignupRequest;
import com.accenture.dhruv.healthdekho.auth.dto.SignupResponse;
import com.accenture.dhruv.healthdekho.auth.service.AuthService;
import com.accenture.dhruv.healthdekho.common.ApiResponse;
import com.accenture.dhruv.healthdekho.config.JwtConfig;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final JwtConfig jwt;

    public AuthController(AuthService auth, JwtConfig jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfResponse>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(HttpStatus.OK, new CsrfResponse(csrfToken.getToken(), csrfToken.getHeaderName())));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        long customerId = auth.authenticate(request);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, jwt.refreshCookie(customerId).toString())
                .body(ApiResponse.success(HttpStatus.OK, new LoginResponse(jwt.createToken(customerId), "Bearer")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(name = JwtConfig.REFRESH_COOKIE, required = false) String refreshToken) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(HttpStatus.OK, auth.refresh(refreshToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, jwt.clearRefreshCookie().toString())
                .body(ApiResponse.success(HttpStatus.OK, new LogoutResponse("Signed out")));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, auth.signup(request)));
    }
}
