package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.HopDong;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
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
