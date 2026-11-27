package com.accenture.dhruv.healthdekho.auth.service;

import com.accenture.dhruv.healthdekho.auth.dto.LoginRequest;
import com.accenture.dhruv.healthdekho.auth.dto.LoginResponse;
import com.accenture.dhruv.healthdekho.auth.dto.SignupRequest;
import com.accenture.dhruv.healthdekho.auth.dto.SignupResponse;
import com.accenture.dhruv.healthdekho.config.JwtConfig;
import com.accenture.dhruv.healthdekho.customer.model.Customer;
import com.accenture.dhruv.healthdekho.customer.service.CustomerService;
import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.accenture.dhruv.healthdekho.exception.UnauthenticatedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private final CustomerService customers;
    private final PasswordEncoder passwords;
    private final JwtConfig jwt;
    private final String dummyHash;

    public AuthService(CustomerService customers, PasswordEncoder passwords, JwtConfig jwt) {
        this.customers = customers;
        this.passwords = passwords;
        this.jwt = jwt;
        this.dummyHash = passwords.encode("unused");
    }

    public long authenticate(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new UnauthenticatedException("credentials", INVALID_CREDENTIALS);
        }
        Customer customer = customers.findByEmail(request.email()).orElse(null);
        String hash = customer == null ? dummyHash : customer.getPasswordHash();
        if (hash.startsWith("{bcrypt}")) {
            hash = hash.substring("{bcrypt}".length());
        }
        if (!passwords.matches(request.password(), hash) || customer == null || !"ACTIVE".equals(customer.getStatus())) {
            throw new UnauthenticatedException("credentials", INVALID_CREDENTIALS);
        }
        return customer.getId();
    }

    public LoginResponse refresh(String refreshToken) {
        var customer = jwt.getRefreshCustomerId(refreshToken)
                .flatMap(customers::findById)
                .filter(account -> "ACTIVE".equals(account.getStatus()))
                .orElseThrow(() -> new UnauthenticatedException("session", "Session expired. Sign in again."));
        return new LoginResponse(jwt.createToken(customer.getId()), "Bearer");
    }

    public SignupResponse signup(SignupRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "password", "Password must fit within 72 UTF-8 bytes");
        }

        Customer customer = new Customer();
        customer.setEmail(request.email());
        customer.setPasswordHash(passwords.encode(request.password()));
        customer.setFullName(request.fullName().trim());
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setPhone(request.phone());
        customer.setStatus("ACTIVE");

        Customer saved = customers.create(customer);
        return new SignupResponse(saved.getId(), saved.getEmail(), saved.getFullName(), saved.getDateOfBirth(),
                saved.getPhone(), saved.getStatus());
    }
}
