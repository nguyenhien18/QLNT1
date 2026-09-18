package com.quanlynhatro.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class CreateChuTroRequest {
    @NotBlank(message = "Ho ten khong duoc de trong")
    @Size(max = 150, message = "Ho ten toi da 150 ky tu")
    private String hoTen;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    @Size(max = 150, message = "Email toi da 150 ky tu")
    private String email;

    @Pattern(regexp = "^$|^[0-9+]{9,20}$", message = "So dien thoai khong hop le")
    @Size(max = 20, message = "So dien thoai toi da 20 ky tu")
    private String sdt;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 6, max = 255, message = "Mat khau phai tu 6 ky tu")
    private String matKhau;
}
