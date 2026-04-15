package com.quanlynhatro.repository;

import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.ChuTro;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ChuTroRepository extends JpaRepository<ChuTro, Long> {
    Optional<ChuTro> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);
    boolean existsByEmailAndChuTroIdNot(String email, Long chuTroId);
    boolean existsBySdtAndChuTroIdNot(String sdt, Long chuTroId);
    Page<ChuTro> findAll(Pageable pageable);
}


