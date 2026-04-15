package com.quanlynhatro.mapper;

import com.quanlynhatro.dto.response.ChiSoResponse;
import com.quanlynhatro.entity.ChiSo;
import org.springframework.stereotype.Component;

@Component
public class ChiSoMapper {
    public ChiSoResponse toResponse(ChiSo entity) {
        return new ChiSoResponse(
                entity.getChiSoId(),
                entity.getPhongTro() == null ? null : entity.getPhongTro().getPhongTroId(),
                entity.getPhongTro() == null ? null : entity.getPhongTro().getTenPhong(),
                entity.getHopDong() == null ? null : entity.getHopDong().getHopDongId(),
                entity.getLoai() == null ? null : entity.getLoai().name(),
                entity.getKy(),
                entity.getThoiDiem(),
                entity.getChiSoCu(),
                entity.getChiSoMoi(),
                entity.getLuongTieuThu(),
                entity.getDonGia(),
                entity.getThanhTien()
        );
    }
}
