package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChuTroResponse {
    private Long chuTroId;
    private String hoTen;
    private String email;
    private String sdt;
}
