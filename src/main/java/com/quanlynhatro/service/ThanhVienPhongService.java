package com.quanlynhatro.service;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.util.PageableUtils;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.entity.ThanhVienPhong;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThanhVienPhongService {
    private final ThanhVienPhongRepository thanhVienPhongRepository;
    private final HopDongRepository hopDongRepository;
    private final KhachThueRepository khachThueRepository;

    public List<ThanhVienPhong> getAll() {
        return thanhVienPhongRepository.findAll();
    }

    public Page<ThanhVienPhong> getPage(Integer page, Integer size, String sortBy, String direction) {
        return thanhVienPhongRepository.findAll(PageableUtils.build(page, size, sortBy, direction, "thanhVienId"));
    }

    public ThanhVienPhong getById(Long id) {
        return thanhVienPhongRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay thanh vien phong"));
    }

    public List<ThanhVienPhong> getByHopDongId(Long hopDongId) {
        return thanhVienPhongRepository.findByHopDong_HopDongId(hopDongId);
    }

    public Page<ThanhVienPhong> getPageByHopDongId(Long hopDongId, Integer page, Integer size, String sortBy, String direction) {
        return thanhVienPhongRepository.findByHopDong_HopDongId(
                hopDongId,
                PageableUtils.build(page, size, sortBy, direction, "thanhVienId")
        );
    }

    public ThanhVienPhong create(ThanhVienPhong thanhVienPhong) {
        HopDong hopDong = requireActiveContract(extractHopDongId(thanhVienPhong));
        Long khachThueId = extractKhachThueId(thanhVienPhong);
        ThanhVienPhong.VaiTro vaiTro = resolveRole(thanhVienPhong.getVaiTro());

        if (vaiTro == ThanhVienPhong.VaiTro.DAI_DIEN && khachThueId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Dai dien phai gan voi mot khach thue");
        }

        if (vaiTro == ThanhVienPhong.VaiTro.DAI_DIEN
                && thanhVienPhongRepository.existsByHopDong_HopDongIdAndVaiTro(hopDong.getHopDongId(), ThanhVienPhong.VaiTro.DAI_DIEN)) {
            throw new AppException(HttpStatus.CONFLICT, "Hop dong nay da co dai dien");
        }

        if (khachThueId != null) {
            KhachThue khachThue = requireActiveTenant(khachThueId);
            if (thanhVienPhongRepository.existsByHopDong_HopDongIdAndKhachThue_KhachThueId(hopDong.getHopDongId(), khachThueId)) {
                throw new AppException(HttpStatus.CONFLICT, "Khach thue nay da co trong hop dong");
            }
            if (thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThaiAndHopDong_HopDongIdNot(
                    khachThueId, HopDong.TrangThai.CON_HIEU_LUC, hopDong.getHopDongId())) {
                throw new AppException(HttpStatus.CONFLICT, "Khach thue da thuoc hop dong active khac");
            }
            fillFromTenant(thanhVienPhong, khachThue);
        }

        validateCapacityOnCreate(hopDong);
        thanhVienPhong.setHopDong(hopDong);
        thanhVienPhong.setVaiTro(vaiTro);
        return thanhVienPhongRepository.save(thanhVienPhong);
    }

    public ThanhVienPhong update(Long id, ThanhVienPhong data) {
        ThanhVienPhong thanhVienPhong = getById(id);
        HopDong oldHopDong = thanhVienPhong.getHopDong();
        Long oldHopDongId = thanhVienPhong.getHopDong() != null ? thanhVienPhong.getHopDong().getHopDongId() : null;
        HopDong targetHopDong = thanhVienPhong.getHopDong();

        if (data.getHopDong() != null && data.getHopDong().getHopDongId() != null) {
            targetHopDong = requireActiveContract(data.getHopDong().getHopDongId());
            thanhVienPhong.setHopDong(targetHopDong);
        }

        ThanhVienPhong.VaiTro newRole = resolveRole(data.getVaiTro());
        if (newRole == ThanhVienPhong.VaiTro.DAI_DIEN
                && !ThanhVienPhong.VaiTro.DAI_DIEN.equals(thanhVienPhong.getVaiTro())
                && targetHopDong != null
                && thanhVienPhongRepository.existsByHopDong_HopDongIdAndVaiTro(targetHopDong.getHopDongId(), ThanhVienPhong.VaiTro.DAI_DIEN)) {
            throw new AppException(HttpStatus.CONFLICT, "Hop dong nay da co dai dien");
        }

        Long targetKhachThueId = data.getKhachThue() != null && data.getKhachThue().getKhachThueId() != null
                ? data.getKhachThue().getKhachThueId()
                : (thanhVienPhong.getKhachThue() != null ? thanhVienPhong.getKhachThue().getKhachThueId() : null);

        if (newRole == ThanhVienPhong.VaiTro.DAI_DIEN && targetKhachThueId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Dai dien phai gan voi mot khach thue");
        }

        boolean leavoingRepresentativeRole = ThanhVienPhong.VaiTro.DAI_DIEN.equals(thanhVienPhong.getVaiTro())
                && (newRole != ThanhVienPhong.VaiTro.DAI_DIEN
                || targetHopDong == null
                || oldHopDongId == null
                || !oldHopDongId.equals(targetHopDong.getHopDongId()));
        if (leavoingRepresentativeRole
                && oldHopDong != null
                && oldHopDongId != null
                && oldHopDong.getTrangThai() == HopDong.TrangThai.CON_HIEU_LUC
                && !thanhVienPhongRepository.existsByHopDong_HopDongIdAndVaiTroAndThanhVienIdNot(
                oldHopDongId, ThanhVienPhong.VaiTro.DAI_DIEN, thanhVienPhong.getThanhVienId())) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the cap nhat voi hop dong cu con hieu luc se khong con dai dien");
        }

        if (targetKhachThueId != null) {
            KhachThue khachThue = requireActiveTenant(targetKhachThueId);
            if (targetHopDong != null
                    && thanhVienPhongRepository.existsByHopDong_HopDongIdAndKhachThue_KhachThueId(targetHopDong.getHopDongId(), targetKhachThueId)
                    && (thanhVienPhong.getKhachThue() == null || !targetKhachThueId.equals(thanhVienPhong.getKhachThue().getKhachThueId()))) {
                throw new AppException(HttpStatus.CONFLICT, "Khach thue nay da co trong hop dong");
            }
            if (targetHopDong != null
                    && thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThaiAndHopDong_HopDongIdNot(
                    targetKhachThueId, HopDong.TrangThai.CON_HIEU_LUC, targetHopDong.getHopDongId())) {
                throw new AppException(HttpStatus.CONFLICT, "Khach thue da thuoc hop dong active khac");
            }
            thanhVienPhong.setKhachThue(khachThue);
            thanhVienPhong.setHoTen(khachThue.getHoTen());
            thanhVienPhong.setSdt(khachThue.getSdt());
            thanhVienPhong.setCccd(khachThue.getCccd());
        } else {
            thanhVienPhong.setHoTen(data.getHoTen());
            thanhVienPhong.setSdt(data.getSdt());
            thanhVienPhong.setCccd(data.getCccd());
        }

        if (newRole == ThanhVienPhong.VaiTro.DAI_DIEN
                && targetHopDong != null
                && (!ThanhVienPhong.VaiTro.DAI_DIEN.equals(thanhVienPhong.getVaiTro())
                || (oldHopDongId != null && !targetHopDong.getHopDongId().equals(oldHopDongId)))
                && thanhVienPhongRepository.existsByHopDong_HopDongIdAndVaiTro(targetHopDong.getHopDongId(), ThanhVienPhong.VaiTro.DAI_DIEN)) {
            throw new AppException(HttpStatus.CONFLICT, "Hop dong nay da co dai dien");
        }

        if (targetHopDong != null && oldHopDongId != null && !targetHopDong.getHopDongId().equals(oldHopDongId)) {
            validateCapacityOnCreate(targetHopDong);
        }

        thanhVienPhong.setVaiTro(newRole);
        return thanhVienPhongRepository.save(thanhVienPhong);
    }

    public void delete(Long id) {
        ThanhVienPhong thanhVienPhong = getById(id);
        if (thanhVienPhong.getVaiTro() == ThanhVienPhong.VaiTro.DAI_DIEN
                && thanhVienPhong.getHopDong() != null
                && thanhVienPhong.getHopDong().getTrangThai() == HopDong.TrangThai.CON_HIEU_LUC) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the xoa dai dien cua hop dong con hieu luc");
        }
        thanhVienPhongRepository.deleteById(id);
    }

    private Long extractHopDongId(ThanhVienPhong thanhVienPhong) {
        if (thanhVienPhong.getHopDong() == null || thanhVienPhong.getHopDong().getHopDongId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hop dong khong duoc de trong");
        }
        return thanhVienPhong.getHopDong().getHopDongId();
    }

    private Long extractKhachThueId(ThanhVienPhong thanhVienPhong) {
        if (thanhVienPhong.getKhachThue() == null) {
            return null;
        }
        return thanhVienPhong.getKhachThue().getKhachThueId();
    }

    private ThanhVienPhong.VaiTro resolveRole(ThanhVienPhong.VaiTro role) {
        return role == null ? ThanhVienPhong.VaiTro.O_CUNG : role;
    }

    private HopDong requireActiveContract(Long hopDongId) {
        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hop dong"));
        if (hopDong.getTrangThai() != HopDong.TrangThai.CON_HIEU_LUC) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chi duoc thao tac thanh vien voi hop dong con hieu luc");
        }
        return hopDong;
    }

    private KhachThue requireActiveTenant(Long khachThueId) {
        KhachThue khachThue = khachThueRepository.findById(khachThueId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay khach thue"));
        if (khachThue.getTrangThai() != KhachThue.TrangThai.HOAT_DONG) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Khach thue '" + khachThue.getHoTen() + "' hien dang bi khoa");
        }
        return khachThue;
    }

    private void validateCapacityOnCreate(HopDong hopDong) {
        Integer maxCapacity = hopDong.getPhongTro() != null ? hopDong.getPhongTro().getSucChua() : null;
        if (maxCapacity == null) {
            return;
        }
        int currentMembers = thanhVienPhongRepository.findByHopDong_HopDongId(hopDong.getHopDongId()).size();
        if (currentMembers + 1 > maxCapacity) {
            throw new AppException(HttpStatus.BAD_REQUEST, "So nguoi o phong vuot qua suc chua toi da cua phong");
        }
    }

    private void fillFromTenant(ThanhVienPhong target, KhachThue khachThue) {
        target.setKhachThue(khachThue);
        if (target.getHoTen() == null || target.getHoTen().isBlank()) {
            target.setHoTen(khachThue.getHoTen());
        }
        if (target.getSdt() == null || target.getSdt().isBlank()) {
            target.setSdt(khachThue.getSdt());
        }
        if (target.getCccd() == null || target.getCccd().isBlank()) {
            target.setCccd(khachThue.getCccd());
        }
    }
}

