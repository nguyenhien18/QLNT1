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
import com.quanlynhatro.entity.ThanhToan;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long>, JpaSpecificationExecutor<ThanhToan> {
    Optional<ThanhToan> findByHoaDon_HoaDonId(Long hoaDonId);
    List<ThanhToan> findByTrangThai(ThanhToan.TrangThai trangThai);
    Page<ThanhToan> findAll(Pageable pageable);
    Page<ThanhToan> findByTrangThai(ThanhToan.TrangThai trangThai, Pageable pageable);
    Page<ThanhToan> findByHoaDon_HopDong_KhachThue_KhachThueId(Long khachThueId, Pageable pageable);
    List<ThanhToan> findByHoaDon_HopDong_KhachThue_KhachThueId(Long khachThueId);

    @Query(value = """
            select distinct tt
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """,
            countQuery = """
            select count(distinct tt)
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """)
    Page<ThanhToan> findAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId, Pageable pageable);

    @Query(value = """
            select distinct tt
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where tt.hoaDon.hopDong.trangThai = :trangThai
              and (tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """,
            countQuery = """
            select count(distinct tt)
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where tt.hoaDon.hopDong.trangThai = :trangThai
              and (tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """)
    Page<ThanhToan> findAccessibleByKhachThueIdAndHopDongTrangThai(@Param("khachThueId") Long khachThueId,
                                                                    @Param("trangThai") HopDong.TrangThai trangThai,
                                                                    Pageable pageable);

    @Query(value = """
            select tt
            from ThanhToan tt
            where (:room = '' or lower(coalesce(tt.hoaDon.phongTro.tenPhong, '')) like concat('%', :room, '%'))
              and (:status is null or tt.trangThai = :status)
              and (:period = '' or lower(coalesce(tt.hoaDon.kyHoaDon, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(tt)
            from ThanhToan tt
            where (:room = '' or lower(coalesce(tt.hoaDon.phongTro.tenPhong, '')) like concat('%', :room, '%'))
              and (:status is null or tt.trangThai = :status)
              and (:period = '' or lower(coalesce(tt.hoaDon.kyHoaDon, '')) like concat('%', :period, '%'))
            """)
    Page<ThanhToan> search(@Param("room") String room,
                           @Param("status") ThanhToan.TrangThai status,
                           @Param("period") String period,
                           Pageable pageable);

    @Query(value = """
            select distinct tt
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where tt.hoaDon.hopDong.trangThai = :hopDongTrangThai
              and (tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or tt.trangThai = :status)
              and (:period = '' or lower(coalesce(tt.hoaDon.kyHoaDon, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(distinct tt)
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where tt.hoaDon.hopDong.trangThai = :hopDongTrangThai
              and (tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or tt.trangThai = :status)
              and (:period = '' or lower(coalesce(tt.hoaDon.kyHoaDon, '')) like concat('%', :period, '%'))
            """)
    Page<ThanhToan> searchAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId,
                                                   @Param("hopDongTrangThai") HopDong.TrangThai hopDongTrangThai,
                                                   @Param("status") ThanhToan.TrangThai status,
                                                   @Param("period") String period,
                                                   Pageable pageable);

    @Query(value = """
            select distinct tt
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where (tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or tt.trangThai = :status)
              and (:period = '' or lower(coalesce(tt.hoaDon.kyHoaDon, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(distinct tt)
            from ThanhToan tt
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = tt.hoaDon.hopDong.hopDongId
            where (tt.hoaDon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or tt.trangThai = :status)
              and (:period = '' or lower(coalesce(tt.hoaDon.kyHoaDon, '')) like concat('%', :period, '%'))
            """)
    Page<ThanhToan> searchAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId,
                                                  @Param("status") ThanhToan.TrangThai status,
                                                  @Param("period") String period,
                                                  Pageable pageable);

    boolean existsByHoaDon_HoaDonId(Long hoaDonId);
    boolean existsByHoaDon_HoaDonIdAndTrangThai(Long hoaDonId, ThanhToan.TrangThai trangThai);
    boolean existsByHoaDon_HopDong_HopDongId(Long hopDongId);
}
