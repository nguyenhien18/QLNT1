package com.quanlynhatro.service;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.util.PageableUtils;
import com.quanlynhatro.dto.request.KhachThueUpdateRequest;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KhachThueService {
    private final KhachThueRepository khachThueRepository;
    private final PasswordEncoder passwordEncoder;
    private final HopDongRepository hopDongRepository;
    private final ThanhVienPhongRepository thanhVienPhongRepository;

    public List<KhachThue> getAll() {
        return khachThueRepository.findAll(Sort.by(Sort.Direction.DESC, "khachThueId"));
    }

    public Page<KhachThue> getPage(Integer page, Integer size, String sortBy, String direction) {
        return khachThueRepository.findAll(PageableUtils.build(page, size, sortBy, direction, "khachThueId"));
    }

    public Page<KhachThue> search(String keyword, Integer page, Integer size) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (q.isBlank()) {
            return getPage(page, size, "khachThueId", "desc");
        }

        Specification<KhachThue> spec = (root, query, cb) -> cb.or(
                cb.like(cb.lower(cb.coalesce(root.<String>get("hoTen"), "")), "%" + q + "%"),
                cb.like(cb.lower(cb.coalesce(root.<String>get("tenDangNhap"), "")), "%" + q + "%"),
                cb.like(cb.lower(cb.coalesce(root.<String>get("cccd"), "")), "%" + q + "%"),
                cb.like(cb.lower(cb.coalesce(root.<String>get("sdt"), "")), "%" + q + "%"),
                cb.like(cb.lower(cb.coalesce(root.<String>get("email"), "")), "%" + q + "%")
        );
        return khachThueRepository.findAll(spec, PageableUtils.build(page, size, "khachThueId", "desc", "khachThueId"));
    }

    public KhachThue getById(Long id) {
        return khachThueRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay khach thue"));
    }

    public KhachThue getByEmail(String email) {
        return khachThueRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay khach thue theo email"));
    }

    public KhachThue getByTenDangNhap(String tenDangNhap) {
        return khachThueRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay khach thue theo ten dang nhap"));
    }

    public KhachThue create(KhachThue khachThue) {
        normalize(khachThue);
        validateUniqueOnCreate(khachThue);
        if (khachThue.getTenDangNhap() == null || khachThue.getTenDangNhap().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ten dang nhap khong duoc de trong");
        }
        if (khachThue.getMatKhau() == null || khachThue.getMatKhau().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mat khau khong duoc de trong");
        }
        if (khachThue.getTrangThai() == null) {
            khachThue.setTrangThai(KhachThue.TrangThai.HOAT_DONG);
        }
        if (khachThue.getCreatedAt() == null) {
            khachThue.setCreatedAt(LocalDateTime.now());
        }
        khachThue.setMatKhau(passwordEncoder.encode(khachThue.getMatKhau()));
        return khachThueRepository.save(khachThue);
    }

    public KhachThue update(Long id, KhachThueUpdateRequest data) {
        KhachThue khachThue = getById(id);
        boolean isRepresentativeInActiveContract = hopDongRepository
                .existsByKhachThue_KhachThueIdAndTrangThai(id, HopDong.TrangThai.CON_HIEU_LUC);
        boolean isMemberInActiveContract = thanhVienPhongRepository
                .existsByKhachThue_KhachThueIdAndHopDong_TrangThai(id, HopDong.TrangThai.CON_HIEU_LUC);
        if (isRepresentativeInActiveContract || isMemberInActiveContract) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the cap nhat khach thue dang thuoc hop dong con hieu luc");
        }

        if (data.getHoTen() != null) {
            String hoTen = trimToNull(data.getHoTen());
            if (hoTen == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Ho ten khong duoc de trong");
            }
            khachThue.setHoTen(hoTen);
        }

        if (data.getCccd() != null) {
            String cccd = trimToNull(data.getCccd());
            if (cccd != null && khachThueRepository.existsByCccdAndKhachThueIdNot(cccd, id)) {
                throw new AppException(HttpStatus.CONFLICT, "CCCD da ton tai");
            }
            khachThue.setCccd(cccd);
        }

        if (data.getSdt() != null) {
            String sdt = trimToNull(data.getSdt());
            if (sdt != null && khachThueRepository.existsBySdtAndKhachThueIdNot(sdt, id)) {
                throw new AppException(HttpStatus.CONFLICT, "So dien thoai da ton tai");
            }
            khachThue.setSdt(sdt);
        }

        if (data.getEmail() != null) {
            String email = trimToNull(data.getEmail());
            if (email != null && khachThueRepository.existsByEmailAndKhachThueIdNot(email, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Email da ton tai");
            }
            khachThue.setEmail(email);
        }

        if (data.getNgaySinh() != null) {
            khachThue.setNgaySinh(data.getNgaySinh());
        }

        if (data.getGioiTinh() != null) {
            khachThue.setGioiTinh(data.getGioiTinh());
        }

        if (data.getDiaChi() != null) {
            khachThue.setDiaChi(trimToNull(data.getDiaChi()));
        }

        if (data.getTenDangNhap() != null && !data.getTenDangNhap().isBlank()) {
            String tenDangNhap = data.getTenDangNhap().trim();
            if (khachThueRepository.existsByTenDangNhapAndKhachThueIdNot(tenDangNhap, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Ten dang nhap da ton tai");
            }
            khachThue.setTenDangNhap(tenDangNhap);
        }

        if (data.getMatKhau() != null && !data.getMatKhau().isBlank()) {
            khachThue.setMatKhau(passwordEncoder.encode(data.getMatKhau().trim()));
        }

        if (data.getTrangThai() != null) {
            khachThue.setTrangThai(data.getTrangThai());
        }

        return khachThueRepository.save(khachThue);
    }

    public void delete(Long id) {
        getById(id);
        if (hopDongRepository.existsByKhachThue_KhachThueId(id) || thanhVienPhongRepository.existsByKhachThue_KhachThueId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the xoa khach thue da co lichu sou hop dong");
        }
        khachThueRepository.deleteById(id);
    }

    private void normalize(KhachThue khachThue) {
        khachThue.setHoTen(trimToNull(khachThue.getHoTen()));
        khachThue.setCccd(trimToNull(khachThue.getCccd()));
        khachThue.setSdt(trimToNull(khachThue.getSdt()));
        khachThue.setEmail(trimToNull(khachThue.getEmail()));
        khachThue.setDiaChi(trimToNull(khachThue.getDiaChi()));
        khachThue.setTenDangNhap(trimToNull(khachThue.getTenDangNhap()));
        khachThue.setMatKhau(trimToNull(khachThue.getMatKhau()));
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateUniqueOnCreate(KhachThue khachThue) {
        if (khachThue.getEmail() != null && khachThueRepository.existsByEmail(khachThue.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email da ton tai");
        }
        if (khachThue.getSdt() != null && khachThueRepository.existsBySdt(khachThue.getSdt())) {
            throw new AppException(HttpStatus.CONFLICT, "So dien thoai da ton tai");
        }
        if (khachThue.getCccd() != null && khachThueRepository.existsByCccd(khachThue.getCccd())) {
            throw new AppException(HttpStatus.CONFLICT, "CCCD da ton tai");
        }
        if (khachThue.getTenDangNhap() != null && khachThueRepository.existsByTenDangNhap(khachThue.getTenDangNhap())) {
            throw new AppException(HttpStatus.CONFLICT, "Ten dang nhap da ton tai");
        }
    }
}


