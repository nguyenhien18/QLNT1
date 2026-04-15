package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HopDongResponse {
    private Long hopDongId;
    private Long phongTroId;
    private String tenPhong;
    private Long khachThueId;
    private String tenKhachThue;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private BigDecimal tienCoc;
    private String trangThai;
}
