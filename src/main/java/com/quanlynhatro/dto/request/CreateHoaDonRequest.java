package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.HoaDon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateHoaDonRequest {
    @NotNull(message = "Hop dong khong duoc de trong")
    private Long hopDongId;

    @NotBlank(message = "Ky hoa don khong duoc de trong")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Ky hoa don phai co dang yyyy-MM")
    private String kyHoaDon;

    private LocalDate ngayLap;
    private HoaDon.TrangThai trangThai;
}