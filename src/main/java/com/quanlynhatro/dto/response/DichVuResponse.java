package com.quanlynhatro.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DichVuResponse {
    private Long dichVuId;
    private String tenDichVu;
    private BigDecimal giaDichVu;
    private String donViTinh;
}

