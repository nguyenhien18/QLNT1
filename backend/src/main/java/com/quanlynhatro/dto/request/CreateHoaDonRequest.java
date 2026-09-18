package com.quanlynhatro.dto.request;

import lombok.Data;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.quanlynhatro.entity.HoaDon;

@Data
public class CreateHoaDonRequest {
    @NotNull(message = "Hop dong khong duoc de trong")
    private Long hopDongId;

    @NotBlank(message = "Ky hoa don khong duoc de trong")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Ky hoa don phai co dang yyyy-MM")
    private String kyHoaDon;

    private LocalDate ngayLap;
    private HoaDon.TrangThai trangThai;
}
