package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String role;
    private String username;
    private String hoTen;
    private String message;
    private String token;
    private String type = "Bearer";

    public LoginResponse(Long id, String role, String username, String hoTen, String message, String token) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.hoTen = hoTen;
        this.message = message;
        this.token = token;
        this.type = "Bearer";
    }
}
