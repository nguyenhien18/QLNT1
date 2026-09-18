package com.quanlynhatro.mapper;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.response.ThanhVienPhongResponse;
import com.quanlynhatro.entity.ThanhVienPhong;

@Component
public class ThanhVienPhongMapper {
    public ThanhVienPhongResponse toThanhVienPhongResponse(ThanhVienPhong entity) {
        return new ThanhVienPhongResponse(
                entity.getThanhVienId(),
                entity.getHopDong() == null ? null : entity.getHopDong().getHopDongId(),
                entity.getKhachThue() == null ? null : entity.getKhachThue().getKhachThueId(),
                entity.getHoTen(),
                entity.getSdt(),
                entity.getCccd(),
                entity.getVaiTro() == null ? null : entity.getVaiTro().name()
        );
    }
}
