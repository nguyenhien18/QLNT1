package com.quanlynhatro.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.quanlynhatro.entity.HopDong;

@Data
public class CreateHopDongRequest {
    @NotNull(message = "Phong tro khong duoc de trong")
    private Long phongTroId;

    @NotNull(message = "Khach thue dai dien khong duoc de trong")
    private Long daiDienKhachThueId;

    private List<Long> thanhVienKhachThueIds = new ArrayList<>();

    @NotNull(message = "Ngay bat dau khong duoc de trong")
    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tien coc phai lon hon hoac bang 0")
    private BigDecimal tienCoc;

    private HopDong.TrangThai trangThai;
}
