package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhongDichVuResponse {
    private Long phongTroId;
    private Long dichVuId;
    private String tenDichVu;
}

