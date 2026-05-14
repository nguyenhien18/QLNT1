package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.ChuTroResponse;
import com.quanlynhatro.entity.ChuTro;

@Component
public class ChuTroMapper {
    public ChuTroResponse toChuTroResponse(ChuTro entity) {
        return new ChuTroResponse(entity.getChuTroId(), entity.getHoTen(), entity.getEmail(), entity.getSdt());
    }
}
