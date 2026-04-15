package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.LoginRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.LoginResponse;
import com.quanlynhatro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends ApiControllerSupport {

    private final AuthService authService;

    @PostMapping("/login/admin")
    public ApiResponse<LoginResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
        return success(authService.loginAdmin(request));
    }

    @PostMapping("/login/user")
    public ApiResponse<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return success(authService.loginUser(request));
    }
}
