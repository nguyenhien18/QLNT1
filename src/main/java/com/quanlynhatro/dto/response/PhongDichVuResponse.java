package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhongDichVuResponse {
    private Long phongTroId;
    private Long dichVuId;
    private String tenDichVu;
}
