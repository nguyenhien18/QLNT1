package com.quanlynhatro.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class ChuTroUpdateRequest {
    @Size(max = 150, message = "Ho ten toi da 150 ky tu")
    private String hoTen;

    @Email(message = "Email khong hop le")
    @Size(max = 150, message = "Email toi da 150 ky tu")
    private String email;

    @Size(max = 20, message = "So dien thoai toi da 20 ky tu")
    private String sdt;

    @Size(min = 6, max = 255, message = "Mat khau phai tu 6 ky tu")
    private String matKhau;
}
