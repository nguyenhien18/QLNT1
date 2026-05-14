package com.quanlynhatro.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.PhongDichVu;
import com.quanlynhatro.entity.PhongDichVuId;

@Repository
public interface PhongDichVuRepository extends JpaRepository<PhongDichVu, PhongDichVuId> {
    List<PhongDichVu> findByPhongTro_PhongTroId(Long phongTroId);
    List<PhongDichVu> findByDichVu_DichVuId(Long dichVuId);
    Page<PhongDichVu> findAll(Pageable pageable);
    Page<PhongDichVu> findByPhongTro_PhongTroId(Long phongTroId, Pageable pageable);
    Page<PhongDichVu> findByDichVu_DichVuId(Long dichVuId, Pageable pageable);
}
