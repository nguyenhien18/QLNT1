package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.PhongDichVuResponse;
import com.quanlynhatro.entity.PhongDichVu;
import org.springframework.stereotype.Component;

@Component
public class PhongDichVuMapper {
    public PhongDichVuResponse toResponse(PhongDichVu entity) {
        return new PhongDichVuResponse(
                entity.getPhongTro() == null ? null : entity.getPhongTro().getPhongTroId(),
                entity.getDichVu() == null ? null : entity.getDichVu().getDichVuId(),
                entity.getDichVu() == null ? null : entity.getDichVu().getTenDichVu()
        );
    }
}
