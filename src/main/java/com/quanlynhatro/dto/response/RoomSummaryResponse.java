package com.quanlynhatro.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RoomSummaryResponse(
        Long phongTroId,
        String tenPhong,
        String loaiPhong,
        BigDecimal giaThue,
        String trangThai,
        Integer sucChua,
        int soNguoiDangO,
        String daiDien,
        List<String> dichVu
) {
}
