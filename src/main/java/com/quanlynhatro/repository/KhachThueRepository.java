package com.quanlynhatro.repository;

import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.KhachThue;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface KhachThueRepository extends JpaRepository<KhachThue, Long>, JpaSpecificationExecutor<KhachThue> {
    Optional<KhachThue> findByEmail(String email);
    Optional<KhachThue> findByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);
    boolean existsByCccd(String cccd);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmailAndKhachThueIdNot(String email, Long khachThueId);
    boolean existsBySdtAndKhachThueIdNot(String sdt, Long khachThueId);
    boolean existsByCccdAndKhachThueIdNot(String cccd, Long khachThueId);
    boolean existsByTenDangNhapAndKhachThueIdNot(String tenDangNhap, Long khachThueId);
    Page<KhachThue> findAll(Pageable pageable);
}


