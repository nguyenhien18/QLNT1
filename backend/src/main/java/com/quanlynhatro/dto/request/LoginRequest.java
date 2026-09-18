package com.quanlynhatro.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    private String username;

    @NotBlank(message = "Mat khau khong duoc de trong")
    private String password;
}
