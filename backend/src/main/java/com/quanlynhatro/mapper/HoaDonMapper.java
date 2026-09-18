package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.HoaDonResponse;
import com.quanlynhatro.entity.HoaDon;

@Component
public class HoaDonMapper {
    public HoaDonResponse toHoaDonResponse(HoaDon entity) {
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
