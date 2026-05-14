package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.PhongTroResponse;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.util.RoomTypeUtils;

@Component
public class PhongTroMapper {
    public PhongTroResponse toPhongTroResponse(PhongTro entity) {
        return new PhongTroResponse(
                entity.getPhongTroId(),
                entity.getChuTro() == null ? null : entity.getChuTro().getChuTroId(),
                entity.getChuTro() == null ? null : entity.getChuTro().getHoTen(),
                entity.getTenPhong(),
                RoomTypeUtils.canonicalizeForRead(entity.getLoaiPhong()),
                entity.getGiaThue(),
                entity.getSucChua(),
                entity.getMoTa(),
                entity.getTrangThai() == null ? null : entity.getTrangThai().name()
        );
    }
}
