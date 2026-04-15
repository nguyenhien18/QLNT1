package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiSoResponse {
    private Long chiSoId;
    private Long phongTroId;
    private String tenPhong;
    private Long hopDongId;
    private String loai;
    private String ky;
    private LocalDate thoiDiem;
    private Integer chiSoCu;
    private Integer chiSoMoi;
    private Integer luongTieuThu;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}
