package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChuTroResponse {
    private Long chuTroId;
    private String hoTen;
    private String email;
    private String sdt;
}

