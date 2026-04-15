package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.HopDongResponse;
import com.quanlynhatro.entity.HopDong;
import org.springframework.stereotype.Component;

@Component
public class HopDongMapper {
    public HopDongResponse toResponse(HopDong entity) {
        return new HopDongResponse(
                entity.getHopDongId(),
                entity.getPhongTro() == null ? null : entity.getPhongTro().getPhongTroId(),
                entity.getPhongTro() == null ? null : entity.getPhongTro().getTenPhong(),
                entity.getKhachThue() == null ? null : entity.getKhachThue().getKhachThueId(),
                entity.getKhachThue() == null ? null : entity.getKhachThue().getHoTen(),
                entity.getNgayBatDau(),
                entity.getNgayKetThuc(),
                entity.getTienCoc(),
                entity.getTrangThai() == null ? null : entity.getTrangThai().name()
        );
    }
}
