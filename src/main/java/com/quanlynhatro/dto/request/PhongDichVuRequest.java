package com.quanlynhatro.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhongDichVuRequest {
    @NotNull(message = "Phong tro khong duoc de trong")
    private Long phongTroId;

    @NotNull(message = "Dich vu khong duoc de trong")
    private Long dichVuId;
}