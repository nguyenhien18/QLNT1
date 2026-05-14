package com.quanlynhatro.dto.request;

import lombok.Data;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.quanlynhatro.entity.KhachThue;

@Data
public class CreateKhachThueRequest {
    @NotBlank(message = "Ho ten khong duoc de trong")
    @Size(max = 150, message = "Ho ten toi da 150 ky tu")
    private String hoTen;

    @Pattern(regexp = "^$|^[0-9]{9,20}$", message = "CCCD chi gom 9-20 chu so")
    @Size(max = 20, message = "CCCD toi da 20 ky tu")
    private String cccd;

    @Pattern(regexp = "^$|^[0-9+]{9,20}$", message = "So dien thoai khong hop le")
    @Size(max = 20, message = "So dien thoai toi da 20 ky tu")
    private String sdt;

    @Email(message = "Email khong hop le")
    @Size(max = 150, message = "Email toi da 150 ky tu")
    private String email;

    private LocalDate ngaySinh;
    private KhachThue.GioiTinh gioiTinh;

    @Size(max = 255, message = "Dia chi toi da 255 ky tu")
    private String diaChi;

    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    @Size(max = 100, message = "Ten dang nhap toi da 100 ky tu")
    private String tenDangNhap;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 6, max = 255, message = "Mat khau phai tu 6 ky tu")
    private String matKhau;

    private KhachThue.TrangThai trangThai;
}
