package com.quanlynhatro.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.quanlynhatro.dto.response.PaymentListItemResponse;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.ThanhToan;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.HoaDonRepository;
import com.quanlynhatro.repository.ThanhToanRepository;
import com.quanlynhatro.util.PageableUtils;

@Service
public class ThanhToanService {
    private final ThanhToanRepository thanhToanRepository;
    private final HoaDonRepository hoaDonRepository;

    public ThanhToanService(ThanhToanRepository thanhToanRepository, HoaDonRepository hoaDonRepository) {
        this.thanhToanRepository = thanhToanRepository;
        this.hoaDonRepository = hoaDonRepository;
    }

    public List<ThanhToan> getAll() {
        return thanhToanRepository.findAll();
    }

    public List<PaymentListItemResponse> getPaymentSummary() {
        Map<Long, ThanhToan> paymentByInvoiceId = thanhToanRepository.findAll().stream()
                .filter(tt -> tt.getHoaDon() != null && tt.getHoaDon().getHoaDonId() != null)
                .collect(Collectors.toMap(
                        tt -> tt.getHoaDon().getHoaDonId(),
                        Function.identity(),
                        (left, right) -> right
                ));

        return hoaDonRepository.findAll().stream()
                .map(invoice -> {
                    ThanhToan payment = paymentByInvoiceId.get(invoice.getHoaDonId());
                    String status = payment != null
                            ? payment.getTrangThai().name()
                            : (invoice.getTrangThai() != null ? invoice.getTrangThai().name() : HoaDon.TrangThai.CHUA_THANH_TOAN.name());
                    return new PaymentListItemResponse(
                            invoice.getHoaDonId(),
                            invoice.getPhongTro() != null ? invoice.getPhongTro().getTenPhong() : "",
                            invoice.getKyHoaDon(),
                            invoice.getTongTien(),
                            status,
                            payment != null ? payment.getNgayThanhToan() : null,
                            payment != null ? payment.getPhuongThuc() : null,
                            payment != null ? payment.getMaGiaoDich() : null
                    );
                })
                .toList();
    }

    public Page<ThanhToan> getPage(Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "thanhToanId");
        return thanhToanRepository.findAll(pageable);
    }

    public Page<ThanhToan> getTenantPayments(Long khachThueId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayThanhToan");
        return thanhToanRepository.findAccessibleByKhachThueId(khachThueId, pageable);
    }

    public Page<ThanhToan> search(String room, String status, String period, Integer page, Integer size) {
        String normalizedRoom = room == null ? "" : room.trim().toLowerCase(Locale.ROOT);
        String normalizedPeriod = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        ThanhToan.TrangThai normalizedStatus = parseSearchStatus(status);
        Pageable pageable = PageableUtils.build(page, size, "ngayThanhToan", "desc", "ngayThanhToan");
        return thanhToanRepository.search(normalizedRoom, normalizedStatus, normalizedPeriod, pageable);
    }

    public Page<ThanhToan> searchTenantPayments(Long khachThueId, String period, String status, Integer page, Integer size) {
        String normalizedPeriod = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        ThanhToan.TrangThai normalizedStatus = parseSearchStatus(status);
        Pageable pageable = PageableUtils.build(page, size, "ngayThanhToan", "desc", "ngayThanhToan");
        return thanhToanRepository.searchAccessibleByKhachThueId(
                khachThueId,
                normalizedStatus,
                normalizedPeriod,
                pageable
        );
    }

    public ThanhToan getById(Long id) {
        return thanhToanRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay thanh toan"));
    }

    public ThanhToan getByHoaDonId(Long hoaDonId) {
        return thanhToanRepository.findByHoaDon_HoaDonId(hoaDonId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay thanh toan theo hoa don"));
    }

    public List<ThanhToan> getByTrangThai(String trangThai) {
        return thanhToanRepository.findByTrangThai(parseTrangThai(trangThai));
    }

    public ThanhToan create(ThanhToan thanhToan) {
        if (thanhToan.getHoaDon() == null || thanhToan.getHoaDon().getHoaDonId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hoa don khong duoc de trong");
        }

        HoaDon hoaDon = hoaDonRepository.findById(thanhToan.getHoaDon().getHoaDonId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));

        if (thanhToanRepository.existsByHoaDon_HoaDonId(hoaDon.getHoaDonId())) {
            throw new AppException(HttpStatus.CONFLICT, "Hoa don nay da co ban ghi thanh toan");
        }

        validatePaymentAmount(thanhToan.getSoTien(), hoaDon.getTongTien(), thanhToan.getTrangThai());
        thanhToan.setHoaDon(hoaDon);
        ThanhToan saved = thanhToanRepository.save(thanhToan);
        syncInvoiceStatus(saved);
        return saved;
    }

    public ThanhToan update(Long id, ThanhToan data) {
        ThanhToan thanhToan = getById(id);
        HoaDon oldHoaDon = thanhToan.getHoaDon();
        Long oldHoaDonId = oldHoaDon != null ? oldHoaDon.getHoaDonId() : null;

        if (data.getHoaDon() != null && data.getHoaDon().getHoaDonId() != null) {
            HoaDon hoaDon = hoaDonRepository.findById(data.getHoaDon().getHoaDonId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));
            if (!hoaDon.getHoaDonId().equals(oldHoaDonId) && thanhToanRepository.existsByHoaDon_HoaDonId(hoaDon.getHoaDonId())) {
                throw new AppException(HttpStatus.CONFLICT, "Hoa don nay da co ban ghi thanh toan");
            }
            thanhToan.setHoaDon(hoaDon);
        }

        BigDecimal soTien = data.getSoTien() != null ? data.getSoTien() : thanhToan.getSoTien();
        ThanhToan.TrangThai trangThai = data.getTrangThai() != null ? data.getTrangThai() : thanhToan.getTrangThai();
        HoaDon targetHoaDon = thanhToan.getHoaDon();
        validatePaymentAmount(soTien, targetHoaDon != null ? targetHoaDon.getTongTien() : null, trangThai);

        if (data.getSoTien() != null) thanhToan.setSoTien(data.getSoTien());
        if (data.getNgayThanhToan() != null) thanhToan.setNgayThanhToan(data.getNgayThanhToan());
        if (data.getPhuongThuc() != null) thanhToan.setPhuongThuc(data.getPhuongThuc());
        if (data.getMaGiaoDich() != null) thanhToan.setMaGiaoDich(data.getMaGiaoDich());
        if (data.getGhiChu() != null) thanhToan.setGhiChu(data.getGhiChu());
        if (data.getTrangThai() != null) thanhToan.setTrangThai(data.getTrangThai());

        ThanhToan saved = thanhToanRepository.save(thanhToan);
        if (oldHoaDonId != null && saved.getHoaDon() != null && !oldHoaDonId.equals(saved.getHoaDon().getHoaDonId())) {
            oldHoaDon.setTrangThai(HoaDon.TrangThai.CHUA_THANH_TOAN);
            hoaDonRepository.save(oldHoaDon);
        }
        syncInvoiceStatus(saved);
        return saved;
    }

    public void delete(Long id) {
        throw new AppException(HttpStatus.CONFLICT, "Khong the xoa thanh toan da ghi nhan");
    }

    public ThanhToan confirmInvoicePaid(Long hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));

        ThanhToan thanhToan = thanhToanRepository.findByHoaDon_HoaDonId(hoaDonId)
                .orElseGet(ThanhToan::new);

        thanhToan.setHoaDon(hoaDon);
        thanhToan.setSoTien(hoaDon.getTongTien());
        thanhToan.setNgayThanhToan(LocalDate.now());
        thanhToan.setPhuongThuc("ADMIN_XAC_NHAN");
        thanhToan.setTrangThai(ThanhToan.TrangThai.THANH_CONG);

        ThanhToan saved = thanhToanRepository.save(thanhToan);
        syncInvoiceStatus(saved);
        return saved;
    }

    private void validatePaymentAmount(BigDecimal soTien, BigDecimal tongTienHoaDon, ThanhToan.TrangThai trangThai) {
        if (soTien == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "So tien thanh toan khong duoc de trong");
        }
        if (tongTienHoaDon == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hoa don chua co tong tien hop le");
        }
        if (trangThai == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trang thai thanh toan khong duoc de trong");
        }
        if (trangThai == ThanhToan.TrangThai.THANH_CONG && soTien.compareTo(tongTienHoaDon) != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Thanh toan thanh cong phai co so tien khop tong tien hoa don");
        }
    }

    private void syncInvoiceStatus(ThanhToan thanhToan) {
        HoaDon hoaDon = thanhToan.getHoaDon();
        if (hoaDon == null) return;
        hoaDon.setTrangThai(thanhToan.getTrangThai() == ThanhToan.TrangThai.THANH_CONG
                ? HoaDon.TrangThai.DA_THANH_TOAN
                : HoaDon.TrangThai.CHUA_THANH_TOAN);
        hoaDonRepository.save(hoaDon);
    }

    private ThanhToan.TrangThai parseTrangThai(String trangThai) {
        try {
            return ThanhToan.TrangThai.valueOf(trangThai);
        } catch (IllegalArgumentException ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trang thai thanh toan khong hop le");
        }
    }
    private ThanhToan.TrangThai parseSearchStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        if ("DA_THANH_TOAN".equals(normalized) || "PAID".equals(normalized)) {
            return ThanhToan.TrangThai.THANH_CONG;
        }
        if ("THAT_BAI".equals(normalized) || "FAILED".equals(normalized)) {
            return ThanhToan.TrangThai.THAT_BAI;
        }
        return parseTrangThai(normalized);
    }
}
