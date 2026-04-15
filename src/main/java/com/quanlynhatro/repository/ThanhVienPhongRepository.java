package com.quanlynhatro.repository;

import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.ThanhVienPhong;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ThanhVienPhongRepository extends JpaRepository<ThanhVienPhong, Long> {
    List<ThanhVienPhong> findByHopDong_HopDongId(Long hopDongId);
    Page<ThanhVienPhong> findAll(Pageable pageable);
    Page<ThanhVienPhong> findByHopDong_HopDongId(Long hopDongId, Pageable pageable);
    boolean existsByHopDong_HopDongIdAndVaiTro(Long hopDongId, ThanhVienPhong.VaiTro vaiTro);
    boolean existsByHopDong_HopDongIdAndVaiTroAndThanhVienIdNot(Long hopDongId, ThanhVienPhong.VaiTro vaiTro, Long thanhVienId);
    boolean existsByHopDong_HopDongIdAndKhachThue_KhachThueId(Long hopDongId, Long khachThueId);
    boolean existsByKhachThue_KhachThueId(Long khachThueId);
    boolean existsByKhachThue_KhachThueIdAndHopDong_TrangThai(Long khachThueId, HopDong.TrangThai trangThai);
    boolean existsByKhachThue_KhachThueIdAndHopDong_TrangThaiAndHopDong_HopDongIdNot(Long khachThueId, HopDong.TrangThai trangThai, Long hopDongId);
    void deleteByHopDong_HopDongId(Long hopDongId);
}


