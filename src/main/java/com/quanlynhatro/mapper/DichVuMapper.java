package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.DichVuResponse;
import com.quanlynhatro.entity.DichVu;
import org.springframework.stereotype.Component;

@Component
public class DichVuMapper {
    public DichVuResponse toResponse(DichVu entity) {
        return new DichVuResponse(entity.getDichVuId(), entity.getTenDichVu(), entity.getGiaDichVu());
    }
}
