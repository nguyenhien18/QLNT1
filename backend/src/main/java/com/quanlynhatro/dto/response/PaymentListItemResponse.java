package com.quanlynhatro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentListItemResponse(
        Long hoaDonId,
        String tenPhong,
        String kyHoaDon,
        BigDecimal tongTien,
        String trangThai,
        LocalDate ngayThanhToan,
        String phuongThuc,
        String maGiaoDich
) {
}
