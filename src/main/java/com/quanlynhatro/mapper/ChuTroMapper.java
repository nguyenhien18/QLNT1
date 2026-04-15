package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.ChuTroResponse;
import com.quanlynhatro.entity.ChuTro;
import org.springframework.stereotype.Component;

@Component
public class ChuTroMapper {
    public ChuTroResponse toResponse(ChuTro entity) {
        return new ChuTroResponse(entity.getChuTroId(), entity.getHoTen(), entity.getEmail(), entity.getSdt());
    }
}
