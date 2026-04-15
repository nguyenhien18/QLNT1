package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.ThanhToanResponse;
import com.quanlynhatro.entity.ThanhToan;
import org.springframework.stereotype.Component;

@Component
public class ThanhToanMapper {
    public ThanhToanResponse toResponse(ThanhToan entity) {
        return new ThanhToanResponse(
                entity.getThanhToanId(),
                entity.getHoaDon() == null ? null : entity.getHoaDon().getHoaDonId(),
                entity.getSoTien(),
                entity.getNgayThanhToan(),
                entity.getPhuongThuc(),
                entity.getMaGiaoDich(),
                entity.getGhiChu(),
                entity.getTrangThai() == null ? null : entity.getTrangThai().name()
        );
    }
}
