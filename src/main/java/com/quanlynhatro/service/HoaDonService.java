package com.quanlynhatro.service;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.util.PageableUtils;
import com.quanlynhatro.dto.request.CreateHoaDonRequest;
import com.quanlynhatro.dto.response.InvoicePreviewResponse;
import com.quanlynhatro.entity.ChiSo;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongDichVu;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.entity.ThanhVienPhong;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChiSoRepository;
import com.quanlynhatro.repository.HoaDonRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.PhongDichVuRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.repository.ThanhToanRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;
    private final HopDongRepository hopDongRepository;
    private final PhongTroRepository phongTroRepository;
    private final ChiSoRepository chiSoRepository;
    private final PhongDichVuRepository phongDichVuRepository;
    private final ThanhVienPhongRepository thanhVienPhongRepository;
    private final ThanhToanRepository thanhToanRepository;

    public List<HoaDon> getAll() {
        return hoaDonRepository.findAll();
    }

    public Page<HoaDon> getPage(Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "hoaDonId");
        return hoaDonRepository.findAll(pageable);
    }

    public HoaDon getById(Long id) {
        return hoaDonRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));
    }

    public List<HoaDon> getByPhongTroId(Long phongTroId) {
        return hoaDonRepository.findByPhongTro_PhongTroId(phongTroId);
    }

    public Page<HoaDon> getPageByPhongTroId(Long phongTroId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayLap");
        return hoaDonRepository.findByPhongTro_PhongTroId(phongTroId, pageable);
    }

    public List<HoaDon> getByHopDongId(Long hopDongId) {
        return hoaDonRepository.findByHopDong_HopDongId(hopDongId);
    }

    public Page<HoaDon> getPageByHopDongId(Long hopDongId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayLap");
        return hoaDonRepository.findByHopDong_HopDongId(hopDongId, pageable);
    }

    public List<HoaDon> getByTrangThai(String trangThai) {
        return hoaDonRepository.findByTrangThai(parseStatus(trangThai));
    }

    public Page<HoaDon> getPageByTrangThai(String trangThai, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayLap");
        return hoaDonRepository.findByTrangThai(parseStatus(trangThai), pageable);
    }

    public List<HoaDon> getByKyHoaDon(String kyHoaDon) {
        return hoaDonRepository.findByKyHoaDon(kyHoaDon);
    }

    public Page<HoaDon> getPageByKyHoaDon(String kyHoaDon, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayLap");
        return hoaDonRepository.findByKyHoaDonContaining(kyHoaDon, pageable);
    }

    public Page<HoaDon> getTenantInvoices(Long khachThueId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayLap");
        return hoaDonRepository.findAccessibleByKhachThueId(khachThueId, pageable);
    }

    public Page<HoaDon> search(String room, String status, String period, Integer page, Integer size) {
        String normalizedRoom = room == null ? "" : room.trim().toLowerCase(Locale.ROOT);
        String normalizedPeriod = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        HoaDon.TrangThai normalizedStatus = parseSearchStatus(status);
        Pageable pageable = PageableUtils.build(page, size, "ngayLap", "desc", "ngayLap");
        return hoaDonRepository.search(normalizedRoom, normalizedStatus, normalizedPeriod, pageable);
    }

    public Page<HoaDon> searchTenantInvoices(Long khachThueId, String period, String status, Integer page, Integer size) {
        String normalizedPeriod = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        HoaDon.TrangThai normalizedStatus = parseSearchStatus(status);
        Pageable pageable = PageableUtils.build(page, size, "ngayLap", "desc", "ngayLap");
        return hoaDonRepository.searchAccessibleByKhachThueId(
                khachThueId,
                normalizedStatus,
                normalizedPeriod,
                pageable
        );
    }

    public InvoicePreviewResponse previewByRoom(Long phongTroId, String kyHoaDon) {
        HopDong hopDong = hopDongRepository.findFirstByPhongTro_PhongTroIdAndTrangThaiOrderByNgayBatDauDesc(phongTroId, HopDong.TrangThai.CON_HIEU_LUC)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Phong nay chua co hop dong con hieu luc"));
        return previewByContract(hopDong.getHopDongId(), kyHoaDon);
    }

    public InvoicePreviewResponse previewByContract(Long hopDongId, String kyHoaDon) {
        HopDong hopDong = getActiveContract(hopDongId);
        PhongTro phongTro = hopDong.getPhongTro();

        BigDecimal tienPhong = defaultZero(phongTro.getGiaThue());
        BigDecimal tienDien = resolveMeterAmount(hopDong, kyHoaDon, ChiSo.Loai.DIEN);
        BigDecimal tienNuoc = resolveMeterAmount(hopDong, kyHoaDon, ChiSo.Loai.NUOC);
        BigDecimal tienDichVu = phongDichVuRepository.findByPhongTro_PhongTroId(phongTro.getPhongTroId()).stream()
                .map(PhongDichVu::getDichVu)
                .map(dv -> defaultZero(dv.getGiaDichVu()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongTien = tienPhong.add(tienDien).add(tienNuoc).add(tienDichVu);

        String daiDien = thanhVienPhongRepository.findByHopDong_HopDongId(hopDongId).stream()
                .filter(tv -> tv.getVaiTro() == ThanhVienPhong.VaiTro.DAI_DIEN)
                .map(ThanhVienPhong::getHoTen)
                .findFirst()
                .orElseGet(() -> hopDong.getKhachThue() != null ? hopDong.getKhachThue().getHoTen() : "");

        return new InvoicePreviewResponse(
                phongTro.getPhongTroId(),
                hopDong.getHopDongId(),
                phongTro.getTenPhong(),
                daiDien,
                kyHoaDon,
                tienPhong,
                tienDien,
                tienNuoc,
                tienDichVu,
                tongTien
        );
    }

    @Transactional
    public HoaDon createFromRequest(CreateHoaDonRequest request) {
        if (hoaDonRepository.existsByHopDong_HopDongIdAndKyHoaDon(request.getHopDongId(), request.getKyHoaDon())) {
            throw new AppException(HttpStatus.CONFLICT, "Hoa don cua hop dong nay theo ky da ton tai");
        }

        InvoicePreviewResponse preview = previewByContract(request.getHopDongId(), request.getKyHoaDon());
        HopDong hopDong = getActiveContract(request.getHopDongId());

        HoaDon hoaDon = new HoaDon();
        hoaDon.setHopDong(hopDong);
        hoaDon.setPhongTro(hopDong.getPhongTro());
        hoaDon.setNgayLap(request.getNgayLap() != null ? request.getNgayLap() : LocalDate.now());
        hoaDon.setKyHoaDon(request.getKyHoaDon());
        hoaDon.setTienPhong(preview.getTienPhong());
        hoaDon.setTienDien(preview.getTienDien());
        hoaDon.setTienNuoc(preview.getTienNuoc());
        hoaDon.setTienDichVu(preview.getTienDichVu());
        hoaDon.setTongTien(preview.getTongTien());
        if (request.getTrangThai() == HoaDon.TrangThai.DA_THANH_TOAN) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the tao hoa don  trang thai  thanh toan khi chua co ban ghi thanh toan");
        }
        hoaDon.setTrangThai(request.getTrangThai() == null ? HoaDon.TrangThai.CHUA_THANH_TOAN : request.getTrangThai());
        return hoaDonRepository.save(hoaDon);
    }

    public HoaDon create(HoaDon hoaDon) {
        if (hoaDon.getHopDong() == null || hoaDon.getHopDong().getHopDongId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hop dong khong duoc de trong");
        }
        if (hoaDon.getPhongTro() == null || hoaDon.getPhongTro().getPhongTroId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Phong tro khong duoc de trong");
        }

        HopDong hopDong = getActiveContract(hoaDon.getHopDong().getHopDongId());

        PhongTro phongTro = phongTroRepository.findById(hoaDon.getPhongTro().getPhongTroId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay phong tro"));

        validateInvoiceRelationship(hopDong, phongTro);
        if (hoaDonRepository.existsByHopDong_HopDongIdAndKyHoaDon(hopDong.getHopDongId(), hoaDon.getKyHoaDon())) {
            throw new AppException(HttpStatus.CONFLICT, "Hoa don cua hop dong nay theo ky da ton tai");
        }

        hoaDon.setHopDong(hopDong);
        hoaDon.setPhongTro(phongTro);
        tinhTongTien(hoaDon);
        if (hoaDon.getTrangThai() == HoaDon.TrangThai.DA_THANH_TOAN) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the tao hoa don  trang thai  thanh toan khi chua co ban ghi thanh toan");
        }
        if (hoaDon.getTrangThai() == null) {
            hoaDon.setTrangThai(HoaDon.TrangThai.CHUA_THANH_TOAN);
        }
        return hoaDonRepository.save(hoaDon);
    }

    public HoaDon update(Long id, HoaDon data) {
        HoaDon hoaDon = getById(id);

        HopDong hopDong = hoaDon.getHopDong();
        if (data.getHopDong() != null && data.getHopDong().getHopDongId() != null) {
            hopDong = getActiveContract(data.getHopDong().getHopDongId());
        }

        PhongTro phongTro = hoaDon.getPhongTro();
        if (data.getPhongTro() != null && data.getPhongTro().getPhongTroId() != null) {
            phongTro = phongTroRepository.findById(data.getPhongTro().getPhongTroId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay phong tro"));
        }

        validateInvoiceRelationship(hopDong, phongTro);
        String kyHoaDon = data.getKyHoaDon() != null ? data.getKyHoaDon() : hoaDon.getKyHoaDon();
        if (hoaDonRepository.existsByHopDong_HopDongIdAndKyHoaDonAndHoaDonIdNot(hopDong.getHopDongId(), kyHoaDon, hoaDon.getHoaDonId())) {
            throw new AppException(HttpStatus.CONFLICT, "Hoa don cua hop dong nay theo ky da ton tai");
        }

        hoaDon.setHopDong(hopDong);
        hoaDon.setPhongTro(phongTro);
        if (data.getNgayLap() != null) hoaDon.setNgayLap(data.getNgayLap());
        hoaDon.setKyHoaDon(kyHoaDon);
        hoaDon.setTienPhong(data.getTienPhong());
        hoaDon.setTienDien(data.getTienDien());
        hoaDon.setTienNuoc(data.getTienNuoc());
        hoaDon.setTienDichVu(data.getTienDichVu());
        tinhTongTien(hoaDon);

        if (data.getTrangThai() != null) {
            if (data.getTrangThai() == HoaDon.TrangThai.DA_THANH_TOAN
                    && !hasSuccessfulPaymentMatchingTotal(hoaDon.getHoaDonId(), hoaDon.getTongTien())) {
                throw new AppException(HttpStatus.CONFLICT, "Khong the cap nhat hoa don sang da thanh toan khi payment thanh cong chua khop tong tien");
            }
            hoaDon.setTrangThai(data.getTrangThai());
        }

        if (hoaDon.getTrangThai() == HoaDon.TrangThai.DA_THANH_TOAN
                && !hasSuccessfulPaymentMatchingTotal(hoaDon.getHoaDonId(), hoaDon.getTongTien())) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the cap nhat hoa don da thanh toan khi tong tien khong khop payment thanh cong");
        }

        return hoaDonRepository.save(hoaDon);
    }

    public HoaDon markAsPaid(Long id) {
        HoaDon hoaDon = getById(id);
        if (!hasSuccessfulPaymentMatchingTotal(id, hoaDon.getTongTien())) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the danh dau da thanh toan khi payment thanh cong chua khop tong tien hoa don");
        }
        hoaDon.setTrangThai(HoaDon.TrangThai.DA_THANH_TOAN);
        return hoaDonRepository.save(hoaDon);
    }

    private boolean hasSuccessfulPaymentMatchingTotal(Long hoaDonId, BigDecimal expectedTotal) {
        return thanhToanRepository.findByHoaDon_HoaDonId(hoaDonId)
                .filter(tt -> tt.getTrangThai() == com.quanlynhatro.entity.ThanhToan.TrangThai.THANH_CONG)
                .map(tt -> defaultZero(tt.getSoTien()).compareTo(defaultZero(expectedTotal)) == 0)
                .orElse(false);
    }

    public void delete(Long id) {
        if (!hoaDonRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don");
        }
        if (thanhToanRepository.existsByHoaDon_HoaDonId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the xoa hoa don  c ban ghi thanh toan");
        }
        hoaDonRepository.deleteById(id);
    }

    private HoaDon.TrangThai parseStatus(String trangThai) {
        try {
            return HoaDon.TrangThai.valueOf(trangThai);
        } catch (IllegalArgumentException ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trang thai hoa don khong hop le");
        }
    }
    private HoaDon.TrangThai parseSearchStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        if ("PAID".equals(normalized)) {
            return HoaDon.TrangThai.DA_THANH_TOAN;
        }
        if ("UNPAID".equals(normalized)) {
            return HoaDon.TrangThai.CHUA_THANH_TOAN;
        }
        return parseStatus(normalized);
    }
    private void tinhTongTien(HoaDon hoaDon) {
        BigDecimal tienPhong = defaultZero(hoaDon.getTienPhong());
        BigDecimal tienDien = defaultZero(hoaDon.getTienDien());
        BigDecimal tienNuoc = defaultZero(hoaDon.getTienNuoc());
        BigDecimal tienDichVu = defaultZero(hoaDon.getTienDichVu());
        hoaDon.setTongTien(tienPhong.add(tienDien).add(tienNuoc).add(tienDichVu));
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal resolveMeterAmount(HopDong hopDong, String kyHoaDon, ChiSo.Loai loai) {
        return chiSoRepository
                .findByHopDong_HopDongIdAndKyAndLoaiOrderByThoiDiemDesc(hopDong.getHopDongId(), kyHoaDon, loai)
                .stream()
                .map(ChiSo::getThanhTien)
                .map(this::defaultZero)
                .findFirst()
                .orElseGet(() -> resolveMeterAmountLegacyByRoom(hopDong, kyHoaDon, loai));
    }

    private BigDecimal resolveMeterAmountLegacyByRoom(HopDong hopDong, String kyHoaDon, ChiSo.Loai loai) {
        LocalDate start = hopDong.getNgayBatDau();
        LocalDate end = hopDong.getNgayKetThuc();
        Long phongTroId = hopDong.getPhongTro() != null ? hopDong.getPhongTro().getPhongTroId() : null;
        if (phongTroId == null) return BigDecimal.ZERO;
        return chiSoRepository
                .findByPhongTro_PhongTroIdAndKyAndLoaiOrderByThoiDiemDesc(phongTroId, kyHoaDon, loai)
                .stream()
                .filter(cs -> cs.getHopDong() == null || cs.getHopDong().getHopDongId() == null)
                .filter(cs -> cs.getThoiDiem() != null)
                .filter(cs -> start == null || !cs.getThoiDiem().isBefore(start))
                .filter(cs -> end == null || !cs.getThoiDiem().isAfter(end))
                .map(ChiSo::getThanhTien)
                .map(this::defaultZero)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private HopDong getActiveContract(Long hopDongId) {
        HopDong hopDong = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hop dong"));
        if (hopDong.getTrangThai() != HopDong.TrangThai.CON_HIEU_LUC) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chi co the lap hoa don cho hop dong con hieu luc");
        }
        if (hopDong.getNgayKetThuc() != null && hopDong.getNgayKetThuc().isBefore(LocalDate.now())) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the lap hoa don cho hop dong da het hieu luc theo ngay ket thuc");
        }
        return hopDong;
    }

    private void validateInvoiceRelationship(HopDong hopDong, PhongTro phongTro) {
        if (hopDong == null || phongTro == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hop dong va phong tro khong duoc de trong");
        }
        Long expectedRoomId = hopDong.getPhongTro() != null ? hopDong.getPhongTro().getPhongTroId() : null;
        if (expectedRoomId == null || !expectedRoomId.equals(phongTro.getPhongTroId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Phong tro khong khop voi hop dong da chon");
        }
    }
}







