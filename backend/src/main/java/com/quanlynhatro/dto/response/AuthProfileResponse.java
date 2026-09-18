package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthProfileResponse {
    private Long id;
    private String role;
    private String username;
    private String hoTen;
}

