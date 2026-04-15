package com.quanlynhatro.repository;

import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.DichVu;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DichVuRepository extends JpaRepository<DichVu, Long> {
    Optional<DichVu> findByTenDichVu(String tenDichVu);
    Page<DichVu> findAll(Pageable pageable);
}


