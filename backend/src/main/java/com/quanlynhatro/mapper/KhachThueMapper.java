package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.KhachThueResponse;
import com.quanlynhatro.entity.KhachThue;

@Component
public class KhachThueMapper {
    public KhachThueResponse toKhachThueResponse(KhachThue entity) {
        return new KhachThueResponse(
                entity.getKhachThueId(),
                entity.getHoTen(),
                entity.getCccd(),
                entity.getSdt(),
                entity.getEmail(),
                entity.getNgaySinh(),
                entity.getGioiTinh() == null ? null : entity.getGioiTinh().name(),
                entity.getDiaChi(),
                entity.getTenDangNhap(),
                entity.getTrangThai() == null ? null : entity.getTrangThai().name(),
                entity.getCreatedAt()
        );
    }
}
