package com.quanlynhatro.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.quanlynhatro.entity.ChiSo;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChiSoRepository;
import com.quanlynhatro.repository.HoaDonRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.util.PageableUtils;

@Service
public class ChiSoService {
    private final ChiSoRepository chiSoRepository;
    private final HopDongRepository hopDongRepository;
    private final HoaDonRepository hoaDonRepository;

    public ChiSoService(ChiSoRepository chiSoRepository, HopDongRepository hopDongRepository, HoaDonRepository hoaDonRepository) {
        this.chiSoRepository = chiSoRepository;
        this.hopDongRepository = hopDongRepository;
        this.hoaDonRepository = hoaDonRepository;
    }

    public List<ChiSo> getAll() {
        return chiSoRepository.findAll();
    }

    public Page<ChiSo> getPage(Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "chiSoId");
        return chiSoRepository.findAll(pageable);
    }

    public Page<ChiSo> getTenantMeters(List<Long> roomIds, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ky");
        if (roomIds == null || roomIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        return chiSoRepository.findByPhongTro_PhongTroIdIn(roomIds, pageable);
    }

    public Page<ChiSo> search(String type, String room, String period, Integer page, Integer size) {
        ChiSo.Loai normalizedType = parseSearchType(type);
        String normalizedRoom = room == null ? "" : room.trim().toLowerCase(Locale.ROOT);
        String normalizedPeriod = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        Pageable pageable = PageableUtils.build(page, size, "ky", "desc", "ky");
        return chiSoRepository.search(normalizedType, normalizedRoom, normalizedPeriod, pageable);
    }

    public Page<ChiSo> searchTenantMeters(List<Long> roomIds, String type, String period, Integer page, Integer size) {
        Pageable pageable = PageableUtils.build(page, size, "ky", "desc", "ky");
        if (roomIds == null || roomIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        ChiSo.Loai normalizedType = parseSearchType(type);
        String normalizedPeriod = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        return chiSoRepository.searchByRoomIds(roomIds, normalizedType, normalizedPeriod, pageable);
    }

    public ChiSo getById(Long id) {
        return chiSoRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay chi so"));
    }

    public List<ChiSo> getByPhongTroId(Long phongTroId) {
        return chiSoRepository.findByPhongTro_PhongTroId(phongTroId);
    }

    public Page<ChiSo> getPageByPhongTroId(Long phongTroId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.build(page, size, sortBy, direction, "ky");
        return chiSoRepository.findByPhongTro_PhongTroId(phongTroId, pageable);
    }

    public List<ChiSo> getByPhongTroIdAndKy(Long phongTroId, String ky) {
        return chiSoRepository.findByPhongTro_PhongTroIdAndKy(phongTroId, ky);
    }

    public List<ChiSo> getByPhongTroIdAndLoai(Long phongTroId, String loai) {
        return chiSoRepository.findByPhongTro_PhongTroIdAndLoai(phongTroId, parseLoai(loai));
    }

    public ChiSo create(ChiSo chiSo) {
        if (chiSo.getThoiDiem() == null) {
            chiSo.setThoiDiem(LocalDate.now());
        }
        HopDong hopDong = resolveAndBindContract(chiSo, null);
        validateMeter(chiSo, hopDong, null);
        validateInvoiceLockByContract(hopDong.getHopDongId(), chiSo.getKy());
        populateMeterFields(chiSo);
        return chiSoRepository.save(chiSo);
    }

    public ChiSo update(Long id, ChiSo data) {
        ChiSo chiSo = getById(id);
        validateMeterNotInvoiced(chiSo);

        HopDong hopDong = resolveAndBindContract(data, chiSo.getHopDong());
        chiSo.setHopDong(hopDong);
        chiSo.setPhongTro(hopDong.getPhongTro());

        chiSo.setLoai(data.getLoai());
        chiSo.setKy(data.getKy());
        chiSo.setChiSoCu(data.getChiSoCu());
        chiSo.setChiSoMoi(data.getChiSoMoi());
        chiSo.setDonGia(data.getDonGia());
        chiSo.setThoiDiem(data.getThoiDiem() != null ? data.getThoiDiem() : chiSo.getThoiDiem());

        validateMeter(chiSo, hopDong, id);
        validateInvoiceLockByContract(hopDong.getHopDongId(), chiSo.getKy());
        populateMeterFields(chiSo);
        return chiSoRepository.save(chiSo);
    }

    public void delete(Long id) {
        ChiSo chiSo = getById(id);
        validateMeterNotInvoiced(chiSo);
        chiSoRepository.deleteById(id);
    }

    private void validateMeterNotInvoiced(ChiSo chiSo) {
        if (chiSo.getHopDong() != null && chiSo.getHopDong().getHopDongId() != null) {
            validateInvoiceLockByContract(chiSo.getHopDong().getHopDongId(), chiSo.getKy());
            return;
        }
        Long roomId = chiSo.getPhongTro() != null ? chiSo.getPhongTro().getPhongTroId() : null;
        validateInvoiceLockByRoom(roomId, chiSo.getKy());
    }

    private void validateInvoiceLockByContract(Long hopDongId, String ky) {
        if (hopDongId == null || ky == null || ky.isBlank()) {
            return;
        }
        if (hoaDonRepository.existsByHopDong_HopDongIdAndKyHoaDon(hopDongId, ky)) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the sua/xoa chi so voi ky nay da co hoa don. Hay xoa hoa don truoc");
        }
    }

    private void validateInvoiceLockByRoom(Long roomId, String ky) {
        if (roomId == null || ky == null || ky.isBlank()) {
            return;
        }
        if (hoaDonRepository.existsByPhongTro_PhongTroIdAndKyHoaDon(roomId, ky)) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the sua/xoa chi so voi ky nay da co hoa don. Hay xoa hoa don truoc");
        }
    }

    private void validateMeter(ChiSo chiSo, HopDong hopDong, Long currentId) {
        validateContractWindow(hopDong, chiSo.getThoiDiem());

        if (chiSo.getChiSoMoi() < chiSo.getChiSoCu()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chi so moi phai lon hon hoac bang chi so cu");
        }
        chiSoRepository.findByHopDong_HopDongIdAndKyAndLoai(hopDong.getHopDongId(), chiSo.getKy(), chiSo.getLoai())
                .filter(existing -> currentId == null || !existing.getChiSoId().equals(currentId))
                .ifPresent(existing -> {
                    throw new AppException(HttpStatus.CONFLICT, "Chi so theo ky va loai nay da ton tai");
                });
    }

    private void validateContractWindow(HopDong hopDong, LocalDate thoiDiem) {
        if (hopDong.getTrangThai() == HopDong.TrangThai.HUY) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the ghi chi so cho hop dong da huy");
        }
        if (hopDong.getNgayKetThuc() != null && hopDong.getNgayKetThuc().isBefore(LocalDate.now())) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the them chi so cho hop dong da het hieu luc theo ngay ket thuc");
        }
        LocalDate meterDate = thoiDiem != null ? thoiDiem : LocalDate.now();
        LocalDate start = hopDong.getNgayBatDau();
        LocalDate end = hopDong.getNgayKetThuc();
        boolean afterStart = start == null || !meterDate.isBefore(start);
        boolean beforeEnd = end == null || !meterDate.isAfter(end);
        if (!(afterStart && beforeEnd)) {
            throw new AppException(HttpStatus.CONFLICT, "Thoi diem chi so khong nam trong thoi gian hieu luc cua hop dong da chon");
        }
    }

    private HopDong resolveAndBindContract(ChiSo input, HopDong currentHopDong) {
        HopDong hopDong = null;
        if (input.getHopDong() != null && input.getHopDong().getHopDongId() != null) {
            hopDong = hopDongRepository.findById(input.getHopDong().getHopDongId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hop dong"));
        } else if (currentHopDong != null && currentHopDong.getHopDongId() != null) {
            hopDong = hopDongRepository.findById(currentHopDong.getHopDongId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hop dong"));
        } else {
            hopDong = inferContractFromLegacyInput(input).orElse(null);
        }

        if (hopDong == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hop dong khong duoc de trong");
        }

        PhongTro contractRoom = hopDong.getPhongTro();
        if (contractRoom == null || contractRoom.getPhongTroId() == null) {
            throw new AppException(HttpStatus.CONFLICT, "Hop dong khong co phong tro hop le");
        }
        if (input.getPhongTro() != null && input.getPhongTro().getPhongTroId() != null
                && !contractRoom.getPhongTroId().equals(input.getPhongTro().getPhongTroId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Phong tro khong khop voi hop dong da chon");
        }

        input.setHopDong(hopDong);
        input.setPhongTro(contractRoom);
        return hopDong;
    }

    private Optional<HopDong> inferContractFromLegacyInput(ChiSo input) {
        Long roomId = input.getPhongTro() != null ? input.getPhongTro().getPhongTroId() : null;
        if (roomId == null) {
            return Optional.empty();
        }
        LocalDate meterDate = input.getThoiDiem() != null ? input.getThoiDiem() : LocalDate.now();
        List<HopDong> candidates = hopDongRepository.findByPhongTro_PhongTroId(roomId).stream()
                .filter(hd -> hd.getTrangThai() != HopDong.TrangThai.HUY)
                .filter(hd -> {
                    LocalDate start = hd.getNgayBatDau();
                    LocalDate end = hd.getNgayKetThuc();
                    boolean afterStart = start == null || !meterDate.isBefore(start);
                    boolean beforeEnd = end == null || !meterDate.isAfter(end);
                    return afterStart && beforeEnd;
                })
                .sorted(Comparator.comparing(HopDong::getNgayBatDau, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        if (candidates.size() > 1) {
            throw new AppException(HttpStatus.CONFLICT, "Khong xac dinh duoc hop dong tu du lieu cu. Vui long gui hopDongId");
        }
        return candidates.stream().findFirst();
    }

    private void populateMeterFields(ChiSo chiSo) {
        chiSo.setLuongTieuThu(chiSo.getChiSoMoi() - chiSo.getChiSoCu());
        if (chiSo.getDonGia() != null) {
            chiSo.setThanhTien(BigDecimal.valueOf(chiSo.getLuongTieuThu()).multiply(chiSo.getDonGia()));
            return;
        }
        chiSo.setThanhTien(null);
    }

    private ChiSo.Loai parseLoai(String loai) {
        try {
            return ChiSo.Loai.valueOf(loai);
        } catch (IllegalArgumentException ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Loai chi so khong hop le");
        }
    }

    private ChiSo.Loai parseSearchType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return null;
        }
        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(normalized)) {
            return null;
        }
        return parseLoai(normalized);
    }
}
