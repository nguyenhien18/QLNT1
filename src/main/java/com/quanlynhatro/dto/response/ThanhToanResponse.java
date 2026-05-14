package com.quanlynhatro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThanhToanResponse {
    private Long thanhToanId;
    private Long hoaDonId;
    private BigDecimal soTien;
    private LocalDate ngayThanhToan;
    private String phuongThuc;
    private String maGiaoDich;
    private String ghiChu;
    private String trangThai;
}

