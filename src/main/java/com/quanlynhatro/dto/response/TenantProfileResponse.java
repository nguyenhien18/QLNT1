package com.quanlynhatro.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.quanlynhatro.entity.KhachThue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProfileResponse {
    private Long khachThueId;
    private String hoTen;
    private String cccd;
    private String sdt;
    private String email;
    private LocalDate ngaySinh;
    private KhachThue.GioiTinh gioiTinh;
    private String diaChi;
    private String tenDangNhap;
    private KhachThue.TrangThai trangThai;
    private LocalDateTime createdAt;

    public static TenantProfileResponse from(KhachThue tenant) {
        return TenantProfileResponse.builder()
                .khachThueId(tenant.getKhachThueId())
                .hoTen(tenant.getHoTen())
                .cccd(tenant.getCccd())
                .sdt(tenant.getSdt())
                .email(tenant.getEmail())
                .ngaySinh(tenant.getNgaySinh())
                .gioiTinh(tenant.getGioiTinh())
                .diaChi(tenant.getDiaChi())
                .tenDangNhap(tenant.getTenDangNhap())
                .trangThai(tenant.getTrangThai())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
