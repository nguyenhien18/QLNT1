package com.quanlynhatro.service;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.util.PageableUtils;
import com.quanlynhatro.dto.request.PhongDichVuRequest;
import com.quanlynhatro.entity.DichVu;
import com.quanlynhatro.entity.PhongDichVu;
import com.quanlynhatro.entity.PhongDichVuId;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.DichVuRepository;
import com.quanlynhatro.repository.PhongDichVuRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhongDichVuService {
    private final PhongDichVuRepository phongDichVuRepository;
    private final PhongTroRepository phongTroRepository;
    private final DichVuRepository dichVuRepository;

    public List<PhongDichVu> getAll() {
        return phongDichVuRepository.findAll();
    }

    public Page<PhongDichVu> getPage(Integer page, Integer size, String sortBy, String direction) {
        return phongDichVuRepository.findAll(PageableUtils.build(page, size, sortBy, direction, "id.phongTroId"));
    }

    public List<PhongDichVu> getByPhongTroId(Long phongTroId) {
        return phongDichVuRepository.findByPhongTro_PhongTroId(phongTroId);
    }

    public Page<PhongDichVu> getPageByPhongTroId(Long phongTroId, Integer page, Integer size, String sortBy, String direction) {
        return phongDichVuRepository.findByPhongTro_PhongTroId(phongTroId, PageableUtils.build(page, size, sortBy, direction, "id.dichVuId"));
    }

    public List<PhongDichVu> getByDichVuId(Long dichVuId) {
        return phongDichVuRepository.findByDichVu_DichVuId(dichVuId);
    }

    public Page<PhongDichVu> getPageByDichVuId(Long dichVuId, Integer page, Integer size, String sortBy, String direction) {
        return phongDichVuRepository.findByDichVu_DichVuId(dichVuId, PageableUtils.build(page, size, sortBy, direction, "id.phongTroId"));
    }

    public PhongDichVu create(PhongDichVuRequest request) {
        PhongTro phongTro = getPhongTro(request.getPhongTroId());
        DichVu dichVu = getDichVu(request.getDichVuId());
        PhongDichVuId id = new PhongDichVuId(request.getPhongTroId(), request.getDichVuId());

        if (phongDichVuRepository.existsById(id)) {
            throw new AppException(HttpStatus.CONFLICT, "Dich vu nay da gan cho phong");
        }

        PhongDichVu phongDichVu = new PhongDichVu();
        phongDichVu.setId(id);
        phongDichVu.setPhongTro(phongTro);
        phongDichVu.setDichVu(dichVu);
        return phongDichVuRepository.save(phongDichVu);
    }

    public void delete(Long phongTroId, Long dichVuId) {
        PhongDichVuId id = new PhongDichVuId(phongTroId, dichVuId);
        if (!phongDichVuRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay dich vu da gan cho phong");
        }
        phongDichVuRepository.deleteById(id);
    }

    private PhongTro getPhongTro(Long phongTroId) {
        return phongTroRepository.findById(phongTroId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay phong tro"));
    }

    private DichVu getDichVu(Long dichVuId) {
        return dichVuRepository.findById(dichVuId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay dich vu"));
    }
}


