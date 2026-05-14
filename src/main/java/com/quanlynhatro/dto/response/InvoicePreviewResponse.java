package com.quanlynhatro.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoicePreviewResponse {
    private Long phongTroId;
    private Long hopDongId;
    private String tenPhong;
    private String daiDien;
    private String kyHoaDon;
    private BigDecimal tienPhong;
    private BigDecimal tienDien;
    private BigDecimal tienNuoc;
    private BigDecimal tienDichVu;
    private BigDecimal tongTien;
}

