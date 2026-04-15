package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.HoaDonResponse;
import com.quanlynhatro.entity.HoaDon;
import org.springframework.stereotype.Component;

@Component
public class HoaDonMapper {
    public HoaDonResponse toResponse(HoaDon entity) {
        return new HoaDonResponse(
                entity.getHoaDonId(),
                entity.getHopDong() == null ? null : entity.getHopDong().getHopDongId(),
                entity.getPhongTro() == null ? null : entity.getPhongTro().getPhongTroId(),
                entity.getPhongTro() == null ? null : entity.getPhongTro().getTenPhong(),
                entity.getNgayLap(),
                entity.getKyHoaDon(),
                entity.getTienPhong(),
                entity.getTienDien(),
                entity.getTienNuoc(),
                entity.getTienDichVu(),
                entity.getTongTien(),
                entity.getTrangThai() == null ? null : entity.getTrangThai().name()
        );
    }
}
