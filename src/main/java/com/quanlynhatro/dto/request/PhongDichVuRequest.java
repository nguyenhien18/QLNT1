package com.quanlynhatro.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class PhongDichVuRequest {
    @NotNull(message = "Phong tro khong duoc de trong")
    private Long phongTroId;

    @NotNull(message = "Dich vu khong duoc de trong")
    private Long dichVuId;
}
