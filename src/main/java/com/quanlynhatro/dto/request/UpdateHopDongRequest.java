package com.quanlynhatro.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;

import com.quanlynhatro.entity.HopDong;

@Data
public class UpdateHopDongRequest {
    private Long phongTroId;
    private Long daiDienKhachThueId;
    private List<Long> thanhVienKhachThueIds;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tien coc phai lon hon hoac bang 0")
    private BigDecimal tienCoc;

    private HopDong.TrangThai trangThai;
}
