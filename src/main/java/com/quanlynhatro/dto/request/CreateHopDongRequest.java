package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.HopDong;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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