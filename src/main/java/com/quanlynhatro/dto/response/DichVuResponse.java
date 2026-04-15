package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DichVuResponse {
    private Long dichVuId;
    private String tenDichVu;
    private BigDecimal giaDichVu;
}
