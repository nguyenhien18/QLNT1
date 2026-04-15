package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.PhongTroResponse;
import com.quanlynhatro.entity.PhongTro;
import org.springframework.stereotype.Component;

@Component
public class PhongTroMapper {
    public PhongTroResponse toResponse(PhongTro entity) {
        return new PhongTroResponse(
                entity.getPhongTroId(),
                entity.getChuTro() == null ? null : entity.getChuTro().getChuTroId(),
                entity.getChuTro() == null ? null : entity.getChuTro().getHoTen(),
                entity.getTenPhong(),
                entity.getLoaiPhong(),
                entity.getGiaThue(),
                entity.getSucChua(),
                entity.getMoTa(),
                entity.getTrangThai() == null ? null : entity.getTrangThai().name()
        );
    }
}
