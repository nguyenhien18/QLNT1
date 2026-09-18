package com.quanlynhatro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HoaDonResponse {
    private Long hoaDonId;
    private Long hopDongId;
    private Long phongTroId;
    private String tenPhong;
    private LocalDate ngayLap;
    private String kyHoaDon;
    private BigDecimal tienPhong;
    private BigDecimal tienDien;
    private BigDecimal tienNuoc;
    private BigDecimal tienDichVu;
    private BigDecimal tongTien;
    private String trangThai;
}

