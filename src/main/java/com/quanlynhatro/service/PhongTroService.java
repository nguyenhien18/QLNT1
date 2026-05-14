package com.quanlynhatro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.quanlynhatro.dto.response.RoomSummaryResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.entity.ThanhVienPhong;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.PhongDichVuRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import com.quanlynhatro.util.PageableUtils;
import com.quanlynhatro.util.RoomTypeUtils;

@Service
public class PhongTroService {
    private final PhongTroRepository phongTroRepository;
    private final ChuTroRepository chuTroRepository;
    private final HopDongRepository hopDongRepository;
    private final ThanhVienPhongRepository thanhVienPhongRepository;
    private final PhongDichVuRepository phongDichVuRepository;

    public PhongTroService(
            PhongTroRepository phongTroRepository,
            ChuTroRepository chuTroRepository,
            HopDongRepository hopDongRepository,
            ThanhVienPhongRepository thanhVienPhongRepository,
            PhongDichVuRepository phongDichVuRepository
    ) {
        this.phongTroRepository = phongTroRepository;
        this.chuTroRepository = chuTroRepository;
        this.hopDongRepository = hopDongRepository;
        this.thanhVienPhongRepository = thanhVienPhongRepository;
        this.phongDichVuRepository = phongDichVuRepository;
    }

    public List<PhongTro> getAll() {
        return phongTroRepository.findAll();
    }

    public List<RoomSummaryResponse> getRoomSummaries() {
        List<PhongTro> rooms = phongTroRepository.findAll();
        List<HopDong> contracts = hopDongRepository.findAll();
        List<ThanhVienPhong> members = thanhVienPhongRepository.findAll();

        Map<Long, HopDong> activeByRoom = contracts.stream()
                .filter(hd -> hd.getTrangThai() == HopDong.TrangThai.CON_HIEU_LUC)
                .filter(hd -> hd.getPhongTro() != null && hd.getPhongTro().getPhongTroId() != null)
                .collect(Collectors.toMap(
                        hd -> hd.getPhongTro().getPhongTroId(),
                        hd -> hd,
                        (left, right) -> right
                ));

        return rooms.stream()
                .map(room -> {
                    Long roomId = room.getPhongTroId();
                    HopDong active = activeByRoom.get(roomId);
                    int occupants = 0;
                    String representative = "";

                    if (active != null && active.getHopDongId() != null) {
                        Long activeContractId = active.getHopDongId();
                        List<ThanhVienPhong> roomMembers = members.stream()
                                .filter(tv -> tv.getHopDong() != null && activeContractId.equals(tv.getHopDong().getHopDongId()))
                                .toList();
                        occupants = roomMembers.size();
                        representative = roomMembers.stream()
                                .filter(tv -> tv.getVaiTro() == ThanhVienPhong.VaiTro.DAI_DIEN)
                                .map(ThanhVienPhong::getHoTen)
                                .findFirst()
                                .orElseGet(() -> active.getKhachThue() != null ? active.getKhachThue().getHoTen() : "");
                    }

                    List<String> services = phongDichVuRepository.findByPhongTro_PhongTroId(roomId).stream()
                            .map(pd -> pd.getDichVu() != null ? pd.getDichVu().getTenDichVu() : null)
                            .filter(name -> name != null && !name.isBlank())
                            .toList();

                    return new RoomSummaryResponse(
                            roomId,
                            room.getTenPhong(),
                            RoomTypeUtils.canonicalizeForRead(room.getLoaiPhong()),
                            room.getGiaThue(),
                            room.getTrangThai() != null ? room.getTrangThai().name() : null,
                            room.getSucChua(),
                            occupants,
                            representative,
                            services
                    );
                })
                .toList();
    }

    public Page<PhongTro> getPage(Integer page, Integer size, String sortBy, String direction) {
        return phongTroRepository.findAll(PageableUtils.build(page, size, sortBy, direction, "phongTroId"));
    }

    public PhongTro getById(Long id) {
        return phongTroRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay phong tro"));
    }

    public List<PhongTro> getByChuTroId(Long chuTroId) {
        return phongTroRepository.findByChuTro_ChuTroId(chuTroId);
    }

    public Page<PhongTro> getPageByChuTroId(Long chuTroId, Integer page, Integer size, String sortBy, String direction) {
        return phongTroRepository.findByChuTro_ChuTroId(chuTroId, PageableUtils.build(page, size, sortBy, direction, "phongTroId"));
    }

    public List<PhongTro> getByTrangThai(String trangThai) {
        return phongTroRepository.findByTrangThai(parseStatus(trangThai));
    }

    public Page<PhongTro> getPageByTrangThai(String trangThai, Integer page, Integer size, String sortBy, String direction) {
        return phongTroRepository.findByTrangThai(
                parseStatus(trangThai),
                PageableUtils.build(page, size, sortBy, direction, "phongTroId")
        );
    }

    public Page<PhongTro> search(String name, String type, String status, String keyword, Integer page, Integer size) {
        String qName = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String qType = normalizeRoomType(type);
        String qStatus = status == null ? "" : status.trim();
        String qKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        Specification<PhongTro> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!qName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.<String>get("tenPhong")), "%" + qName + "%"));
            }
            if (!qType.isBlank()) {
                predicates.add(cb.equal(cb.upper(cb.coalesce(root.<String>get("loaiPhong"), "")), qType));
            }
            if (!qStatus.isBlank()) {
                predicates.add(cb.equal(root.get("trangThai"), parseStatus(qStatus)));
            }
            if (!qKeyword.isBlank()) {
                var roomName = cb.lower(cb.coalesce(root.<String>get("tenPhong"), ""));
                var roomType = cb.lower(cb.coalesce(root.<String>get("loaiPhong"), ""));
                predicates.add(cb.or(
                        cb.like(roomName, "%" + qKeyword + "%"),
                        cb.like(roomType, "%" + qKeyword + "%")
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return phongTroRepository.findAll(spec, PageableUtils.build(page, size, "phongTroId", "desc", "phongTroId"));
    }

    public PhongTro create(PhongTro phongTro) {
        phongTro.setChuTro(resolveChuTro(phongTro));
        phongTro.setLoaiPhong(requireRoomType(phongTro.getLoaiPhong()));
        if (phongTro.getTrangThai() == null) {
            phongTro.setTrangThai(PhongTro.TrangThai.TRONG);
        } else if (phongTro.getTrangThai() == PhongTro.TrangThai.DA_CHO_THUE) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Khong the tao phong o trang thai da cho thue khi chua co hop dong");
        }
        return phongTroRepository.save(phongTro);
    }

    public PhongTro update(Long id, PhongTro data) {
        PhongTro existingPhongTro = getById(id);
        if (hasActiveContract(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Phong dang co hop dong hieu luc, khong duoc sua thong tin phong");
        }
        validateStatusChange(id, data.getTrangThai());

        if (hasChuTroId(data)) {
            existingPhongTro.setChuTro(resolveChuTro(data));
        }

        applyMutableFields(existingPhongTro, data);
        return phongTroRepository.save(existingPhongTro);
    }

    public void delete(Long id) {
        getById(id);
        if (hopDongRepository.existsByPhongTro_PhongTroId(id)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "Khong the xoa phong da tung co hop dong. Hay ket thuc/huy hop dong va giu lich su phong"
            );
        }
        phongTroRepository.deleteById(id);
    }

    private void applyMutableFields(PhongTro target, PhongTro source) {
        target.setTenPhong(source.getTenPhong());
        target.setLoaiPhong(requireRoomType(source.getLoaiPhong()));
        target.setGiaThue(source.getGiaThue());
        target.setSucChua(source.getSucChua());
        target.setMoTa(source.getMoTa());
        target.setTrangThai(source.getTrangThai());
    }

    private ChuTro resolveChuTro(PhongTro phongTro) {
        if (!hasChuTroId(phongTro)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Thieu thong tin chu tro");
        }

        Long chuTroId = phongTro.getChuTro().getChuTroId();
        return chuTroRepository.findById(chuTroId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay chu tro"));
    }

    private boolean hasChuTroId(PhongTro phongTro) {
        return phongTro.getChuTro() != null && phongTro.getChuTro().getChuTroId() != null;
    }

    private void validateStatusChange(Long phongTroId, PhongTro.TrangThai requestedStatus) {
        if (requestedStatus == null) {
            return;
        }
        boolean activeContract = hasActiveContract(phongTroId);
        if (requestedStatus == PhongTro.TrangThai.DA_CHO_THUE && !activeContract) {
            throw new AppException(HttpStatus.CONFLICT, "Phong khong co hop dong hieu luc, khong the set da cho thue");
        }
        if (requestedStatus != PhongTro.TrangThai.TRONG) {
            return;
        }
        if (activeContract) {
            throw new AppException(HttpStatus.CONFLICT, "Phong dang con hop dong hieu luc, khong the set phong trong");
        }
    }

    private boolean hasActiveContract(Long phongTroId) {
        return hopDongRepository.existsByPhongTro_PhongTroIdAndTrangThai(
                phongTroId,
                HopDong.TrangThai.CON_HIEU_LUC
        );
    }

    private PhongTro.TrangThai parseStatus(String trangThai) {
        try {
            return PhongTro.TrangThai.valueOf(trangThai);
        } catch (IllegalArgumentException ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trang thai phong khong hop le");
        }
    }

    private String normalizeRoomType(String roomType) {
        String normalized = RoomTypeUtils.normalizeSupported(roomType);
        if (normalized.isBlank() && roomType != null && !roomType.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Loai phong chi duoc la THUONG hoac VIP");
        }
        return normalized;
    }

    private String requireRoomType(String roomType) {
        String normalized = normalizeRoomType(roomType);
        if (normalized.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Loai phong chi duoc la THUONG hoac VIP");
        }
        return normalized;
    }
}
