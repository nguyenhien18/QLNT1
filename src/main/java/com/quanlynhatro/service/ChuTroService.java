package com.quanlynhatro.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.quanlynhatro.dto.request.ChuTroUpdateRequest;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.util.PageableUtils;

@Service
public class ChuTroService {
    private final ChuTroRepository chuTroRepository;
    private final PhongTroRepository phongTroRepository;
    private final PasswordEncoder passwordEncoder;

    public ChuTroService(ChuTroRepository chuTroRepository, PhongTroRepository phongTroRepository, PasswordEncoder passwordEncoder) {
        this.chuTroRepository = chuTroRepository;
        this.phongTroRepository = phongTroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ChuTro> getAll() {
        return chuTroRepository.findAll(Sort.by(Sort.Direction.DESC, "chuTroId"));
    }

    public Page<ChuTro> getPage(Integer page, Integer size, String sortBy, String direction) {
        return chuTroRepository.findAll(PageableUtils.build(page, size, sortBy, direction, "chuTroId"));
    }

    public ChuTro getById(Long id) {
        return chuTroRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay chu tro"));
    }

    public ChuTro getByEmail(String email) {
        return chuTroRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Khong tim thay chu tro voi email: " + email));
    }

    public ChuTro create(ChuTro chuTro) {
        normalize(chuTro);
        validateUniqueOnCreate(chuTro);

        if (chuTro.getMatKhau() == null || chuTro.getMatKhau().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mat khau khong duoc de trong");
        }

        chuTro.setMatKhau(passwordEncoder.encode(chuTro.getMatKhau()));
        return chuTroRepository.save(chuTro);
    }

    public ChuTro update(Long id, ChuTroUpdateRequest data) {
        ChuTro chuTro = getById(id);

        if (data.getHoTen() != null) {
            String hoTen = trimToNull(data.getHoTen());
            if (hoTen == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Ho ten khong duoc de trong");
            }
            chuTro.setHoTen(hoTen);
        }

        if (data.getEmail() != null) {
            String email = trimToNull(data.getEmail());
            if (email != null && chuTroRepository.existsByEmailAndChuTroIdNot(email, id)) {
                throw new AppException(HttpStatus.CONFLICT, "Email da ton tai");
            }
            chuTro.setEmail(email);
        }

        if (data.getSdt() != null) {
            String sdt = trimToNull(data.getSdt());
            if (sdt != null && chuTroRepository.existsBySdtAndChuTroIdNot(sdt, id)) {
                throw new AppException(HttpStatus.CONFLICT, "So dien thoai da ton tai");
            }
            chuTro.setSdt(sdt);
        }

        if (data.getMatKhau() != null && !data.getMatKhau().isBlank()) {
            chuTro.setMatKhau(passwordEncoder.encode(data.getMatKhau().trim()));
        }

        return chuTroRepository.save(chuTro);
    }

    public void delete(Long id) {
        getById(id);
        if (phongTroRepository.existsByChuTro_ChuTroId(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Khong the xoa chu tro da co phong tro");
        }
        chuTroRepository.deleteById(id);
    }

    private void validateUniqueOnCreate(ChuTro chuTro) {
        if (chuTro.getEmail() != null && chuTroRepository.existsByEmail(chuTro.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "Email da ton tai");
        }
        if (chuTro.getSdt() != null && chuTroRepository.existsBySdt(chuTro.getSdt())) {
            throw new AppException(HttpStatus.CONFLICT, "So dien thoai da ton tai");
        }
    }

    private void normalize(ChuTro chuTro) {
        chuTro.setHoTen(trimToNull(chuTro.getHoTen()));
        chuTro.setEmail(trimToNull(chuTro.getEmail()));
        chuTro.setSdt(trimToNull(chuTro.getSdt()));
        chuTro.setMatKhau(trimToNull(chuTro.getMatKhau()));
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
