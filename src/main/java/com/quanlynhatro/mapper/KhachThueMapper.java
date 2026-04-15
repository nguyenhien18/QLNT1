package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.KhachThueResponse;
import com.quanlynhatro.entity.KhachThue;
import org.springframework.stereotype.Component;

@Component
public class KhachThueMapper {
    public KhachThueResponse toResponse(KhachThue entity) {
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
