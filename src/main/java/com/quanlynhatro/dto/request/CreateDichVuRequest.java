package com.quanlynhatro.dto.request;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CreateDichVuRequest {
    @NotBlank(message = "Ten dich vu khong duoc de trong")
    @Size(max = 100, message = "Ten dich vu toi da 100 ky tu")
    private String tenDichVu;

    @NotNull(message = "Gia dich vu khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Gia dich vu phai lon hon hoac bang 0")
    private BigDecimal giaDichVu;

    @Size(max = 50, message = "Don vi tinh toi da 50 ky tu")
    private String donViTinh;
}
