package com.accenture.dhruv.healthdekho.auth.controller;

import com.accenture.dhruv.healthdekho.auth.dto.LoginRequest;
import com.accenture.dhruv.healthdekho.auth.dto.LoginResponse;
import com.accenture.dhruv.healthdekho.auth.dto.SignupRequest;
import com.accenture.dhruv.healthdekho.auth.dto.SignupResponse;
import com.accenture.dhruv.healthdekho.auth.service.AuthService;
import com.accenture.dhruv.healthdekho.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(HttpStatus.OK, auth.login(request)));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, auth.signup(request)));
    }
}
