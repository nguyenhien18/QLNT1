package com.quanlynhatro.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KhachThueResponse {
    private Long khachThueId;
    private String hoTen;
    private String cccd;
    private String sdt;
    private String email;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private String tenDangNhap;
    private String trangThai;
    private LocalDateTime createdAt;
}

