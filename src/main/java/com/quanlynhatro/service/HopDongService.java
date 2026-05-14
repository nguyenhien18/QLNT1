package com.quanlynhatro.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlynhatro.dto.request.CreateHopDongRequest;
import com.quanlynhatro.dto.request.UpdateHopDongRequest;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.entity.ThanhVienPhong;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.HoaDonRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.repository.ThanhToanRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import com.quanlynhatro.util.PageableUtils;

@Service
public class HopDongService {
    private final HopDongRepository hopDongRepository;
    private final PhongTroRepository phongTroRepository;
    private final KhachThueRepository khachThueRepository;
    private final ThanhVienPhongRepository thanhVienPhongRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;

    public HopDongService(
            HopDongRepository hopDongRepository,
            PhongTroRepository phongTroRepository,
            KhachThueRepository khachThueRepository,
            ThanhVienPhongRepository thanhVienPhongRepository,
            HoaDonRepository hoaDonRepository,
            ThanhToanRepository thanhToanRepository
    ) {
        this.hopDongRepository = hopDongRepository;
        this.phongTroRepository = phongTroRepository;
        this.khachThueRepository = khachThueRepository;
        this.thanhVienPhongRepository = thanhVienPhongRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanRepository = thanhToanRepository;
    }

    public List<HopDong> getAll() {
        return hopDongRepository.findAll();
    }

    public Page<HopDong> getPage(Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "hopDongId");
        return hopDongRepository.findAll(pageable);
    }

    public HopDong getById(Long id) {
        return hopDongRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hop dong"));
    }

    public List<HopDong> getByPhongTroId(Long phongTroId) {
        return hopDongRepository.findByPhongTro_PhongTroId(phongTroId);
    }

    public Page<HopDong> getPageByPhongTroId(Long phongTroId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayBatDau");
        return hopDongRepository.findByPhongTro_PhongTroId(phongTroId, pageable);
    }

    public List<HopDong> getByKhachThueId(Long khachThueId) {
        return hopDongRepository.findByKhachThue_KhachThueId(khachThueId);
    }

    public Page<HopDong> getPageByKhachThueId(Long khachThueId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayBatDau");
        return hopDongRepository.findByKhachThue_KhachThueId(khachThueId, pageable);
    }

    public Page<HopDong> getTenantContracts(Long khachThueId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayBatDau");
        return hopDongRepository.findAccessibleByKhachThueId(khachThueId, pageable);
    }

    public List<Long> getTenantRoomIds(Long khachThueId) {
        return hopDongRepository.findAccessibleByKhachThueIdAndTrangThai(khachThueId, HopDong.TrangThai.CON_HIEU_LUC).stream()
                .map(hd -> hd.getPhongTro() != null ? hd.getPhongTro().getPhongTroId() : null)
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    public HopDong getCurrentTenantContract(Long khachThueId) {
        return hopDongRepository.findAccessibleByKhachThueIdAndTrangThai(khachThueId, HopDong.TrangThai.CON_HIEU_LUC).stream()
                .sorted(Comparator.comparing(HopDong::getNgayBatDau, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .findFirst()
                .orElseGet(() -> hopDongRepository.findAccessibleByKhachThueId(khachThueId).stream()
                        .sorted(Comparator.comparing(HopDong::getNgayBatDau, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .findFirst()
                        .orElse(null));
    }

    public List<HopDong> getByTrangThai(String trangThai) {
        return hopDongRepository.findByTrangThai(parseStatus(trangThai));
    }

    public Page<HopDong> getPageByTrangThai(String trangThai, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ngayBatDau");
        return hopDongRepository.findByTrangThai(parseStatus(trangThai), pageable);
    }

    public Page<HopDong> search(String room, String status, Integer page, Integer size) {
        String qRoom = room == null ? "" : room.trim().toLowerCase(Locale.ROOT);
        String qStatus = status == null ? "" : status.trim();

        Specification<HopDong> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!qRoom.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.join("phongTro").<String>get("tenPhong"), "")),
                        "%" + qRoom + "%"
                ));
            }
            if (!qStatus.isBlank()) {
                predicates.add(cb.equal(root.get("trangThai"), parseStatus(qStatus)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return hopDongRepository.findAll(spec, PageableUtils.build(page, size, "ngayBatDau", "desc", "ngayBatDau"));
    }

    public List<PhongTro> getAvailableRooms() {
        return phongTroRepository.findAll().stream()
                .filter(room -> room.getPhongTroId() != null)
                .filter(room -> !hopDongRepository.existsByPhongTro_PhongTroIdAndTrangThai(
                        room.getPhongTroId(), HopDong.TrangThai.CON_HIEU_LUC))
                .toList();
    }

    public List<KhachThue> getAvailableTenants(Long currentHopDongId) {
        return khachThueRepository.findAll().stream()
                .filter(tenant -> tenant.getKhachThueId() != null)
                .filter(tenant -> tenant.getTrangThai() == KhachThue.TrangThai.HOAT_DONG)
                .filter(tenant -> isTenantAvailableForContract(tenant.getKhachThueId(), currentHopDongId))
                .sorted(Comparator.comparing(KhachThue::getKhachThueId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public HopDong createFromRequest(CreateHopDongRequest request) {
        PhongTro phongTro = phongTroRepository.findById(request.getPhongTroId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay phong tro"));

        if (hopDongRepository.findFirstByPhongTro_PhongTroIdAndTrangThaiOrderByNgayBatDauDesc(
                phongTro.getPhongTroId(), HopDong.TrangThai.CON_HIEU_LUC).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "Phong nay dang co hop dong con hieu luc");
        }

        KhachThue daiDien = requireActiveTenant(request.getDaiDienKhachThueId(), "Khong tim thay khach thue dai dien");
        validateDateRange(request.getNgayBatDau(), request.getNgayKetThuc());
        validateMembers(phongTro, request.getDaiDienKhachThueId(), request.getThanhVienKhachThueIds(), null);

        HopDong hopDong = new HopDong();
        hopDong.setPhongTro(phongTro);
        hopDong.setKhachThue(daiDien);
        hopDong.setNgayBatDau(request.getNgayBatDau());
        hopDong.setNgayKetThuc(request.getNgayKetThuc());
        hopDong.setTienCoc(request.getTienCoc());
        hopDong.setTrangThai(HopDong.TrangThai.CON_HIEU_LUC);

        HopDong saved = hopDongRepository.save(hopDong);
        syncMembers(saved, request.getDaiDienKhachThueId(), request.getThanhVienKhachThueIds());
        updateRoomStatus(saved.getPhongTro(), saved.getTrangThai());
        return saved;
    }

    @Transactional
    public HopDong update(Long id, UpdateHopDongRequest request) {
        HopDong hopDong = getById(id);
        Long oldRoomId = hopDong.getPhongTro() != null ? hopDong.getPhongTro().getPhongTroId() : null;
        validateNoHistoricalFinanceMutation(hopDong, request);

        LocalDate effectiveNgayBatDau = request.getNgayBatDau() != null ? request.getNgayBatDau() : hopDong.getNgayBatDau();
        LocalDate effectiveNgayKetThuc = request.getNgayKetThuc() != null ? request.getNgayKetThuc() : hopDong.getNgayKetThuc();
        validateDateRange(effectiveNgayBatDau, effectiveNgayKetThuc);

        if (request.getPhongTroId() != null && (oldRoomId == null || !oldRoomId.equals(request.getPhongTroId()))) {
            PhongTro phongTro = phongTroRepository.findById(request.getPhongTroId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay phong tro"));
            if (hopDongRepository.findFirstByPhongTro_PhongTroIdAndTrangThaiOrderByNgayBatDauDesc(
                    phongTro.getPhongTroId(), HopDong.TrangThai.CON_HIEU_LUC)
                    .filter(existing -> !existing.getHopDongId().equals(id)).isPresent()) {
                throw new AppException(HttpStatus.CONFLICT, "Phong moi dang co hop dong con hieu luc");
            }
            hopDong.setPhongTro(phongTro);
        }

        if (request.getDaiDienKhachThueId() != null) {
            KhachThue khachThue = requireActiveTenant(request.getDaiDienKhachThueId(), "Khong tim thay khach thue");
            hopDong.setKhachThue(khachThue);
        }

        if (request.getNgayBatDau() != null) hopDong.setNgayBatDau(request.getNgayBatDau());
        if (request.getNgayKetThuc() != null) hopDong.setNgayKetThuc(request.getNgayKetThuc());
        if (request.getTienCoc() != null) hopDong.setTienCoc(request.getTienCoc());
        if (request.getTrangThai() != null && request.getTrangThai() != hopDong.getTrangThai()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Khong duoc cap nhat trang thai hop dong tai endpoint nay");
        }

        Long daiDienId = request.getDaiDienKhachThueId() != null
                ? request.getDaiDienKhachThueId()
                : hopDong.getKhachThue().getKhachThueId();

        List<Long> companionIds = resolveCompanionIds(hopDong, request.getThanhVienKhachThueIds(), daiDienId);
        PhongTro currentRoom = hopDong.getPhongTro();
        validateMembers(currentRoom, daiDienId, companionIds, hopDong.getHopDongId());

        HopDong saved = hopDongRepository.save(hopDong);
        syncMembers(saved, daiDienId, companionIds);
        syncRoomStatus(saved.getPhongTro() != null ? saved.getPhongTro().getPhongTroId() : null);
        if (oldRoomId != null && (saved.getPhongTro() == null || !oldRoomId.equals(saved.getPhongTro().getPhongTroId()))) {
            syncRoomStatus(oldRoomId);
        }
        return saved;
    }

    @Transactional
    public HopDong ketThucHopDong(Long id) {
        HopDong hopDong = getById(id);
        if (hopDong.getTrangThai() != HopDong.TrangThai.CON_HIEU_LUC) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chi co the ket thuc hop dong con hieu luc");
        }
        LocalDate today = LocalDate.now();
        if (hopDong.getNgayBatDau() != null && hopDong.getNgayBatDau().isAfter(today)) {
            throw new AppException(HttpStatus.CONFLICT, "Hop dong chua den ngay hieu luc, vui long huy hop dong");
        }
        hopDong.setTrangThai(HopDong.TrangThai.HET_HIEU_LUC);
        HopDong saved = hopDongRepository.save(hopDong);
        syncRoomStatus(saved.getPhongTro() != null ? saved.getPhongTro().getPhongTroId() : null);
        return saved;
    }

    @Transactional
    public HopDong huyHopDong(Long id) {
        HopDong hopDong = getById(id);
        if (hopDong.getTrangThai() != HopDong.TrangThai.CON_HIEU_LUC) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chi co the huy hop dong con hieu luc");
        }
        LocalDate today = LocalDate.now();
        if (hopDong.getNgayBatDau() != null && !hopDong.getNgayBatDau().isAfter(today)) {
            throw new AppException(HttpStatus.CONFLICT, "Hop dong da co hieu luc, vui long ket thuc hop dong thay voi huy");
        }
        hopDong.setTrangThai(HopDong.TrangThai.HUY);
        HopDong saved = hopDongRepository.save(hopDong);
        syncRoomStatus(saved.getPhongTro() != null ? saved.getPhongTro().getPhongTroId() : null);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        HopDong hopDong = getById(id);
        if (thanhToanRepository.existsByHoaDon_HopDong_HopDongId(id) || hoaDonRepository.existsByHopDong_HopDongId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the xoa hop dong da phat sinh hoa don hoac thanh toan");
        }
        Long phongTroId = hopDong.getPhongTro() != null ? hopDong.getPhongTro().getPhongTroId() : null;
        thanhVienPhongRepository.deleteByHopDong_HopDongId(id);
        hopDongRepository.deleteById(id);
        syncRoomStatus(phongTroId);
    }

    private HopDong.TrangThai parseStatus(String trangThai) {
        try {
            return HopDong.TrangThai.valueOf(trangThai);
        } catch (IllegalArgumentException ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trang thai hop dong khong hop le");
        }
    }

    private void validateDateRange(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        if (ngayKetThuc != null && ngayBatDau != null && ngayKetThuc.isBefore(ngayBatDau)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ngay ket thuc phai sau hoac bang ngay bat dau");
        }
    }

    private void updateRoomStatus(PhongTro phongTro, HopDong.TrangThai trangThai) {
        if (phongTro == null) return;
        phongTro.setTrangThai(trangThai == HopDong.TrangThai.CON_HIEU_LUC ? PhongTro.TrangThai.DA_CHO_THUE : PhongTro.TrangThai.TRONG);
        phongTroRepository.save(phongTro);
    }

    private void syncRoomStatus(Long phongTroId) {
        if (phongTroId == null) return;
        phongTroRepository.findById(phongTroId).ifPresent(room -> {
            boolean occupied = hopDongRepository.findByPhongTro_PhongTroId(phongTroId).stream()
                    .anyMatch(hd -> hd.getTrangThai() == HopDong.TrangThai.CON_HIEU_LUC);
            room.setTrangThai(occupied ? PhongTro.TrangThai.DA_CHO_THUE : PhongTro.TrangThai.TRONG);
            phongTroRepository.save(room);
        });
    }

    private List<Long> resolveCompanionIds(HopDong hopDong, List<Long> requestedCompanionIds, Long daiDienKhachThueId) {
        if (requestedCompanionIds != null) {
            return requestedCompanionIds.stream()
                    .filter(id -> id != null && !id.equals(daiDienKhachThueId))
                    .toList();
        }
        return thanhVienPhongRepository.findByHopDong_HopDongId(hopDong.getHopDongId()).stream()
                .filter(member -> member.getVaiTro() == ThanhVienPhong.VaiTro.O_CUNG)
                .map(member -> member.getKhachThue() != null ? member.getKhachThue().getKhachThueId() : null)
                .filter(id -> id != null && !id.equals(daiDienKhachThueId))
                .toList();
    }

    private void validateNoHistoricalFinanceMutation(HopDong hopDong, UpdateHopDongRequest request) {
        if (hopDong.getTrangThai() != HopDong.TrangThai.CON_HIEU_LUC) {
            return;
        }

        Long hopDongId = hopDong.getHopDongId();
        if (hopDongId == null) {
            return;
        }

        boolean hasFinancialHistory = hoaDonRepository.existsByHopDong_HopDongId(hopDongId)
                || thanhToanRepository.existsByHoaDon_HopDong_HopDongId(hopDongId);
        if (!hasFinancialHistory) {
            return;
        }

        if (isRoomChanged(hopDong, request)
                || isRepresentativeChanged(hopDong, request)
                || isCompanionListChanged(hopDong, request)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "Hop dong da phat sinh hoa don/thanh toan, khong duoc doi phong, doi dai dien hoac danh sach thanh vien"
            );
        }
    }

    private boolean isRoomChanged(HopDong hopDong, UpdateHopDongRequest request) {
        if (request.getPhongTroId() == null) {
            return false;
        }
        Long currentRoomId = hopDong.getPhongTro() != null ? hopDong.getPhongTro().getPhongTroId() : null;
        return !request.getPhongTroId().equals(currentRoomId);
    }

    private boolean isRepresentativeChanged(HopDong hopDong, UpdateHopDongRequest request) {
        if (request.getDaiDienKhachThueId() == null) {
            return false;
        }
        Long currentRepresentativeId = hopDong.getKhachThue() != null ? hopDong.getKhachThue().getKhachThueId() : null;
        return !request.getDaiDienKhachThueId().equals(currentRepresentativeId);
    }

    private boolean isCompanionListChanged(HopDong hopDong, UpdateHopDongRequest request) {
        if (request.getThanhVienKhachThueIds() == null) {
            return false;
        }
        Long representativeId = request.getDaiDienKhachThueId() != null
                ? request.getDaiDienKhachThueId()
                : (hopDong.getKhachThue() != null ? hopDong.getKhachThue().getKhachThueId() : null);
        Set<Long> requestedCompanionIds = new HashSet<>(request.getThanhVienKhachThueIds().stream()
                .filter(id -> id != null && !id.equals(representativeId))
                .toList());
        Set<Long> currentCompanionIds = new HashSet<>(thanhVienPhongRepository.findByHopDong_HopDongId(hopDong.getHopDongId()).stream()
                .filter(member -> member.getVaiTro() == ThanhVienPhong.VaiTro.O_CUNG)
                .map(member -> member.getKhachThue() != null ? member.getKhachThue().getKhachThueId() : null)
                .filter(id -> id != null)
                .toList());
        return !requestedCompanionIds.equals(currentCompanionIds);
    }

    private void validateMembers(PhongTro phongTro, Long daiDienKhachThueId, List<Long> thanhVienKhachThueIds, Long currentHopDongId) {
        Set<Long> allIds = new LinkedHashSet<>();
        allIds.add(daiDienKhachThueId);
        if (thanhVienKhachThueIds != null) {
            allIds.addAll(thanhVienKhachThueIds.stream()
                    .filter(id -> id != null && !id.equals(daiDienKhachThueId))
                    .toList());
        }

        int maxCapacity = phongTro != null && phongTro.getSucChua() != null ? phongTro.getSucChua() : Integer.MAX_VALUE;
        if (allIds.size() > maxCapacity) {
            throw new AppException(HttpStatus.BAD_REQUEST, "So nguoi o phong vuot qua suc chua toi da cua phong");
        }

        for (Long khachThueId : allIds) {
            KhachThue kt = requireActiveTenant(khachThueId, "Khong tim thay khach thue");
            boolean occupied = currentHopDongId == null
                    ? thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThai(
                    khachThueId,
                    HopDong.TrangThai.CON_HIEU_LUC
            )
                    : thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThaiAndHopDong_HopDongIdNot(
                    khachThueId,
                    HopDong.TrangThai.CON_HIEU_LUC,
                    currentHopDongId
            );
            if (occupied) {
                throw new AppException(HttpStatus.CONFLICT, "Khach thue '" + kt.getHoTen() + "' da thuoc phong/hop dong khac");
            }
        }
    }

    private void syncMembers(HopDong hopDong, Long daiDienKhachThueId, List<Long> thanhVienKhachThueIds) {
        thanhVienPhongRepository.deleteByHopDong_HopDongId(hopDong.getHopDongId());
        Set<Long> memberIds = new LinkedHashSet<>();
        memberIds.add(daiDienKhachThueId);
        if (thanhVienKhachThueIds != null) {
            memberIds.addAll(thanhVienKhachThueIds.stream()
                    .filter(id -> id != null && !id.equals(daiDienKhachThueId))
                    .toList());
        }

        for (Long memberId : memberIds) {
            KhachThue khachThue = requireActiveTenant(memberId, "Khong tim thay khach thue trong danh sach thanh vien");
            ThanhVienPhong member = new ThanhVienPhong();
            member.setHopDong(hopDong);
            member.setKhachThue(khachThue);
            member.setHoTen(khachThue.getHoTen());
            member.setSdt(khachThue.getSdt());
            member.setCccd(khachThue.getCccd());
            member.setVaiTro(memberId.equals(daiDienKhachThueId)
                    ? ThanhVienPhong.VaiTro.DAI_DIEN
                    : ThanhVienPhong.VaiTro.O_CUNG);
            thanhVienPhongRepository.save(member);
        }
    }

    private KhachThue requireActiveTenant(Long khachThueId, String notFoundMessage) {
        KhachThue khachThue = khachThueRepository.findById(khachThueId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, notFoundMessage));
        if (khachThue.getTrangThai() != KhachThue.TrangThai.HOAT_DONG) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Khach thue '" + khachThue.getHoTen() + "' hien dang bi khoa");
        }
        return khachThue;
    }

    private boolean isTenantAvailableForContract(Long khachThueId, Long currentHopDongId) {
        if (currentHopDongId == null) {
            boolean representativeInActive = hopDongRepository.existsByKhachThue_KhachThueIdAndTrangThai(
                    khachThueId, HopDong.TrangThai.CON_HIEU_LUC
            );
            boolean memberInActive = thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThai(
                    khachThueId, HopDong.TrangThai.CON_HIEU_LUC
            );
            return !representativeInActive && !memberInActive;
        }

        boolean representativeInOtherActive = hopDongRepository.existsByKhachThue_KhachThueIdAndTrangThaiAndHopDongIdNot(
                khachThueId, HopDong.TrangThai.CON_HIEU_LUC, currentHopDongId
        );
        boolean memberInOtherActive = thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThaiAndHopDong_HopDongIdNot(
                khachThueId, HopDong.TrangThai.CON_HIEU_LUC, currentHopDongId
        );
        return !representativeInOtherActive && !memberInOtherActive;
    }
}
