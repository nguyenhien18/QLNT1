package com.quanlynhatro.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.quanlynhatro.dto.request.CreateChiSoRequest;
import com.quanlynhatro.dto.request.UpdateChiSoRequest;
import com.quanlynhatro.entity.ChiSo;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;

@Component
public class ChiSoRequestMapper {
    public ChiSo toEntity(CreateChiSoRequest request) {
        return toEntity(
                request.getPhongTroId(),
                request.getHopDongId(),
                request.getLoai(),
                request.getKy(),
                request.getThoiDiem(),
                request.getChiSoCu(),
                request.getChiSoMoi(),
                request.getLuongTieuThu(),
                request.getDonGia(),
                request.getThanhTien()
        );
    }

    public ChiSo toEntity(UpdateChiSoRequest request) {
        return toEntity(
                request.getPhongTroId(),
                request.getHopDongId(),
                request.getLoai(),
                request.getKy(),
                request.getThoiDiem(),
                request.getChiSoCu(),
                request.getChiSoMoi(),
                request.getLuongTieuThu(),
                request.getDonGia(),
                request.getThanhTien()
        );
    }

    private ChiSo toEntity(Long phongTroId,
                           Long hopDongId,
                           ChiSo.Loai loai,
                           String ky,
                           LocalDate thoiDiem,
                           Integer chiSoCu,
                           Integer chiSoMoi,
                           Integer luongTieuThu,
                           BigDecimal donGia,
                           BigDecimal thanhTien) {
        ChiSo entity = new ChiSo();
        entity.setPhongTro(toPhongTro(phongTroId));
        entity.setHopDong(toHopDong(hopDongId));
        entity.setLoai(loai);
        entity.setKy(ky);
        entity.setThoiDiem(thoiDiem);
        entity.setChiSoCu(chiSoCu);
        entity.setChiSoMoi(chiSoMoi);
        entity.setLuongTieuThu(luongTieuThu);
        entity.setDonGia(donGia);
        entity.setThanhTien(thanhTien);
        return entity;
    }

    private PhongTro toPhongTro(Long id) {
        if (id == null) {
            return null;
        }
        PhongTro phongTro = new PhongTro();
        phongTro.setPhongTroId(id);
        return phongTro;
    }

    private HopDong toHopDong(Long id) {
        if (id == null) {
            return null;
        }
        HopDong hopDong = new HopDong();
        hopDong.setHopDongId(id);
        return hopDong;
    }
}
