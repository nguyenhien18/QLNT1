package com.quanlynhatro.controller;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlynhatro.dto.request.LoginRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.AuthProfileResponse;
import com.quanlynhatro.dto.response.LoginResponse;
import com.quanlynhatro.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final String AUTH_COOKIE = "QLPT_AUTH";

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostMapping("/login/admin")
    public ApiResponse<LoginResponse> loginAdmin(@Valid @RequestBody LoginRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        LoginResponse loginResponse = authService.loginAdmin(request);
        writeAuthCookie(httpResponse, loginResponse.getToken(), httpRequest.isSecure());
        return ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("success")
                .result(loginResponse)
                .build();
    }

    @PostMapping("/login/user")
    public ApiResponse<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        LoginResponse loginResponse = authService.loginUser(request);
        writeAuthCookie(httpResponse, loginResponse.getToken(), httpRequest.isSecure());
        return ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("success")
                .result(loginResponse)
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<AuthProfileResponse> me() {
        return ApiResponse.<AuthProfileResponse>builder()
                .code(200)
                .message("success")
                .result(authService.getCurrentProfile())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        clearAuthCookie(httpResponse, httpRequest.isSecure());
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    private void writeAuthCookie(HttpServletResponse response, String token, boolean secure) {
        if (token == null || token.isBlank()) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(Math.max(jwtExpirationMs, 0)))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAuthCookie(HttpServletResponse response, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

