package com.quanlynhatro.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.DichVu;

@Repository
public interface DichVuRepository extends JpaRepository<DichVu, Long> {
    Optional<DichVu> findByTenDichVu(String tenDichVu);
    Page<DichVu> findAll(Pageable pageable);
}
