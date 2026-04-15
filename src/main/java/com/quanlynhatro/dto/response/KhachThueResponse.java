package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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
