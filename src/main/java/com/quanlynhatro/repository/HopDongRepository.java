package com.quanlynhatro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.HopDong;

@Repository
public interface HopDongRepository extends JpaRepository<HopDong, Long>, JpaSpecificationExecutor<HopDong> {
    List<HopDong> findByPhongTro_PhongTroId(Long phongTroId);
    boolean existsByPhongTro_PhongTroId(Long phongTroId);
    boolean existsByPhongTro_PhongTroIdAndTrangThai(Long phongTroId, HopDong.TrangThai trangThai);
    boolean existsByKhachThue_KhachThueId(Long khachThueId);
    boolean existsByKhachThue_KhachThueIdAndTrangThai(Long khachThueId, HopDong.TrangThai trangThai);
    boolean existsByKhachThue_KhachThueIdAndTrangThaiAndHopDongIdNot(Long khachThueId, HopDong.TrangThai trangThai, Long hopDongId);
    List<HopDong> findByKhachThue_KhachThueId(Long khachThueId);
    List<HopDong> findByTrangThai(HopDong.TrangThai trangThai);
    Optional<HopDong> findFirstByPhongTro_PhongTroIdAndTrangThaiOrderByNgayBatDauDesc(Long phongTroId, HopDong.TrangThai trangThai);

    Page<HopDong> findAll(Pageable pageable);
    Page<HopDong> findByPhongTro_PhongTroId(Long phongTroId, Pageable pageable);
    Page<HopDong> findByKhachThue_KhachThueId(Long khachThueId, Pageable pageable);
    Page<HopDong> findByTrangThai(HopDong.TrangThai trangThai, Pageable pageable);

    @Query("""
            select distinct hd
            from HopDong hd
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hd.hopDongId
            where hd.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """)
    List<HopDong> findAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId);

    @Query(value = """
            select distinct hd
            from HopDong hd
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hd.hopDongId
            where hd.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """,
            countQuery = """
            select count(distinct hd)
            from HopDong hd
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hd.hopDongId
            where hd.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """)
    Page<HopDong> findAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId, Pageable pageable);

    @Query("""
            select distinct hd
            from HopDong hd
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hd.hopDongId
            where hd.trangThai = :trangThai
              and (hd.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """)
    List<HopDong> findAccessibleByKhachThueIdAndTrangThai(@Param("khachThueId") Long khachThueId,
                                                          @Param("trangThai") HopDong.TrangThai trangThai);

    @Query(value = """
            select distinct hd
            from HopDong hd
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hd.hopDongId
            where hd.trangThai = :trangThai
              and (hd.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """,
            countQuery = """
            select count(distinct hd)
            from HopDong hd
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hd.hopDongId
            where hd.trangThai = :trangThai
              and (hd.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """)
    Page<HopDong> findAccessibleByKhachThueIdAndTrangThai(@Param("khachThueId") Long khachThueId,
                                                          @Param("trangThai") HopDong.TrangThai trangThai,
                                                          Pageable pageable);
}
