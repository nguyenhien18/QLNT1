package com.quanlynhatro.repository;

import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.PhongTro;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface PhongTroRepository extends JpaRepository<PhongTro, Long>, JpaSpecificationExecutor<PhongTro> {
    List<PhongTro> findByChuTro_ChuTroId(Long chuTroId);
    boolean existsByChuTro_ChuTroId(Long chuTroId);
    List<PhongTro> findByTrangThai(PhongTro.TrangThai trangThai);
    Page<PhongTro> findAll(Pageable pageable);
    Page<PhongTro> findByChuTro_ChuTroId(Long chuTroId, Pageable pageable);
    Page<PhongTro> findByTrangThai(PhongTro.TrangThai trangThai, Pageable pageable);
}


