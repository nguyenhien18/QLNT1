package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.PhongDichVuResponse;
import com.quanlynhatro.entity.PhongDichVu;

@Component
public class PhongDichVuMapper {
    public PhongDichVuResponse toPhongDichVuResponse(PhongDichVu entity) {
        return new PhongDichVuResponse(
                entity.getPhongTro() == null ? null : entity.getPhongTro().getPhongTroId(),
                entity.getDichVu() == null ? null : entity.getDichVu().getDichVuId(),
                entity.getDichVu() == null ? null : entity.getDichVu().getTenDichVu()
        );
    }
}
