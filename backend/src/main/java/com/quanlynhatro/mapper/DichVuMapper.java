package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.DichVuResponse;
import com.quanlynhatro.entity.DichVu;

@Component
public class DichVuMapper {
    public DichVuResponse toDichVuResponse(DichVu entity) {
        return new DichVuResponse(
                entity.getDichVuId(),
                entity.getTenDichVu(),
                entity.getGiaDichVu(),
                entity.getDonViTinh()
        );
    }
}
