package com.quanlynhatro.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.quanlynhatro.entity.DichVu;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.DichVuRepository;
import com.quanlynhatro.util.PageableUtils;

@Service
public class DichVuService {
    private final DichVuRepository dichVuRepository;

    public DichVuService(DichVuRepository dichVuRepository) {
        this.dichVuRepository = dichVuRepository;
    }

    public List<DichVu> getAll() {
        return dichVuRepository.findAll();
    }

    public Page<DichVu> getPage(Integer page, Integer size, String sortBy, String direction) {
        return dichVuRepository.findAll(PageableUtils.build(page, size, sortBy, direction, "dichVuId"));
    }

    public DichVu getById(Long id) {
        return dichVuRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay dich vu"));
    }

    public DichVu create(DichVu dichVu) {
        normalize(dichVu);
        return dichVuRepository.save(dichVu);
    }

    public DichVu update(Long id, DichVu data) {
        DichVu dichVu = getById(id);
        applyChanges(dichVu, data);
        return dichVuRepository.save(dichVu);
    }

    public void delete(Long id) {
        getById(id);
        dichVuRepository.deleteById(id);
    }

    private void applyChanges(DichVu target, DichVu source) {
        target.setTenDichVu(source.getTenDichVu());
        target.setGiaDichVu(source.getGiaDichVu());
        target.setDonViTinh(trimToNull(source.getDonViTinh()));
    }

    private void normalize(DichVu dichVu) {
        dichVu.setTenDichVu(trimToNull(dichVu.getTenDichVu()));
        dichVu.setDonViTinh(trimToNull(dichVu.getDonViTinh()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
