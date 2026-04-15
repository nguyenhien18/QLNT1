package com.quanlynhatro.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "khach_thue")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KhachThue {

    public enum GioiTinh { NAM, NU, KHAC }
    public enum TrangThai { HOAT_DONG, KHOA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "khach_thue_id")
    private Long khachThueId;

    @NotBlank(message = "Ho ten khong duoc de trong")
    @Size(max = 150, message = "Ho ten toi da 150 ky tu")
    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Size(max = 20, message = "CCCD toi da 20 ky tu")
    @Pattern(regexp = "^$|^[0-9]{9,20}$", message = "CCCD chi gom 9-20 chu so")
    @Column(name = "cccd", unique = true, length = 20)
    private String cccd;

    @Size(max = 20, message = "So dien thoai toi da 20 ky tu")
    @Pattern(regexp = "^$|^[0-9+]{9,20}$", message = "So dien thoai khong hop le")
    @Column(name = "sdt", unique = true, length = 20)
    private String sdt;

    @Email(message = "Email khong hop le")
    @Size(max = 150, message = "Email toi da 150 ky tu")
    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Enumerated(EnumType.STRING)
    @Column(name = "gioi_tinh")
    private GioiTinh gioiTinh;

    @Size(max = 255, message = "Dia chi toi da 255 ky tu")
    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    @Size(max = 100, message = "Ten dang nhap toi da 100 ky tu")
    @Column(name = "ten_dang_nhap", unique = true, nullable = false, length = 100)
    private String tenDangNhap;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, max = 255, message = "Mat khau phai tu 6 ky tu")
    @Column(name = "mat_khau", nullable = false, length = 255)
    private String matKhau;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThai trangThai = TrangThai.HOAT_DONG;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}




