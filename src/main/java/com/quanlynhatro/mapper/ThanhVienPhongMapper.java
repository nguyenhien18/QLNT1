package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.ThanhVienPhongResponse;
import com.quanlynhatro.entity.ThanhVienPhong;
import org.springframework.stereotype.Component;

@Component
public class ThanhVienPhongMapper {
    public ThanhVienPhongResponse toResponse(ThanhVienPhong entity) {
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
