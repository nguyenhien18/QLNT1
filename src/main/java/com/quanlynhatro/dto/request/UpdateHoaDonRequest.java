package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.HoaDon;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateHoaDonRequest {
    private Long hopDongId;
    private Long phongTroId;
    private LocalDate ngayLap;
    private String kyHoaDon;
    private BigDecimal tienPhong;
    private BigDecimal tienDien;
    private BigDecimal tienNuoc;
    private BigDecimal tienDichVu;
    private BigDecimal tongTien;
    private HoaDon.TrangThai trangThai;
}
