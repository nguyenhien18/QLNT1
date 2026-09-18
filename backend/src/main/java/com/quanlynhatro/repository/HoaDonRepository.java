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

import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.HopDong;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {
    List<HoaDon> findByPhongTro_PhongTroId(Long phongTroId);
    List<HoaDon> findByHopDong_HopDongId(Long hopDongId);
    List<HoaDon> findByTrangThai(HoaDon.TrangThai trangThai);
    long countByTrangThai(HoaDon.TrangThai trangThai);
    List<HoaDon> findByKyHoaDon(String kyHoaDon);

    Page<HoaDon> findAll(Pageable pageable);
    Page<HoaDon> findByPhongTro_PhongTroId(Long phongTroId, Pageable pageable);
    Page<HoaDon> findByHopDong_HopDongId(Long hopDongId, Pageable pageable);
    Page<HoaDon> findByTrangThai(HoaDon.TrangThai trangThai, Pageable pageable);
    Page<HoaDon> findByKyHoaDonContaining(String kyHoaDon, Pageable pageable);
    Page<HoaDon> findByHopDong_KhachThue_KhachThueId(Long khachThueId, Pageable pageable);
    List<HoaDon> findByHopDong_KhachThue_KhachThueId(Long khachThueId);

    @Query(value = """
            select distinct hdon
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where hdon.hopDong.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """,
            countQuery = """
            select count(distinct hdon)
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where hdon.hopDong.khachThue.khachThueId = :khachThueId
               or tv.khachThue.khachThueId = :khachThueId
            """)
    Page<HoaDon> findAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId, Pageable pageable);

    @Query(value = """
            select distinct hdon
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where hdon.hopDong.trangThai = :trangThai
              and (hdon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """,
            countQuery = """
            select count(distinct hdon)
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where hdon.hopDong.trangThai = :trangThai
              and (hdon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
            """)
    Page<HoaDon> findAccessibleByKhachThueIdAndHopDongTrangThai(@Param("khachThueId") Long khachThueId,
                                                                 @Param("trangThai") HopDong.TrangThai trangThai,
                                                                 Pageable pageable);

    @Query(value = """
            select hdon
            from HoaDon hdon
            where (:room = '' or lower(coalesce(hdon.phongTro.tenPhong, '')) like concat('%', :room, '%'))
              and (:status is null or hdon.trangThai = :status)
              and (:period = '' or lower(coalesce(hdon.kyHoaDon, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(hdon)
            from HoaDon hdon
            where (:room = '' or lower(coalesce(hdon.phongTro.tenPhong, '')) like concat('%', :room, '%'))
              and (:status is null or hdon.trangThai = :status)
              and (:period = '' or lower(coalesce(hdon.kyHoaDon, '')) like concat('%', :period, '%'))
            """)
    Page<HoaDon> search(@Param("room") String room,
                        @Param("status") HoaDon.TrangThai status,
                        @Param("period") String period,
                        Pageable pageable);

    @Query(value = """
            select distinct hdon
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where hdon.hopDong.trangThai = :hopDongTrangThai
              and (hdon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or hdon.trangThai = :status)
              and (:period = '' or lower(coalesce(hdon.kyHoaDon, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(distinct hdon)
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where hdon.hopDong.trangThai = :hopDongTrangThai
              and (hdon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or hdon.trangThai = :status)
              and (:period = '' or lower(coalesce(hdon.kyHoaDon, '')) like concat('%', :period, '%'))
            """)
    Page<HoaDon> searchAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId,
                                               @Param("hopDongTrangThai") HopDong.TrangThai hopDongTrangThai,
                                               @Param("status") HoaDon.TrangThai status,
                                               @Param("period") String period,
                                               Pageable pageable);

    @Query(value = """
            select distinct hdon
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where (hdon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or hdon.trangThai = :status)
              and (:period = '' or lower(coalesce(hdon.kyHoaDon, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(distinct hdon)
            from HoaDon hdon
            left join ThanhVienPhong tv on tv.hopDong.hopDongId = hdon.hopDong.hopDongId
            where (hdon.hopDong.khachThue.khachThueId = :khachThueId
                   or tv.khachThue.khachThueId = :khachThueId)
              and (:status is null or hdon.trangThai = :status)
              and (:period = '' or lower(coalesce(hdon.kyHoaDon, '')) like concat('%', :period, '%'))
            """)
    Page<HoaDon> searchAccessibleByKhachThueId(@Param("khachThueId") Long khachThueId,
                                               @Param("status") HoaDon.TrangThai status,
                                               @Param("period") String period,
                                               Pageable pageable);

    boolean existsByHopDong_HopDongIdAndKyHoaDon(Long hopDongId, String kyHoaDon);
    boolean existsByHopDong_HopDongIdAndKyHoaDonAndHoaDonIdNot(Long hopDongId, String kyHoaDon, Long hoaDonId);
    Optional<HoaDon> findByHopDong_HopDongIdAndKyHoaDon(Long hopDongId, String kyHoaDon);
    boolean existsByHopDong_HopDongId(Long hopDongId);
    boolean existsByPhongTro_PhongTroIdAndKyHoaDon(Long phongTroId, String kyHoaDon);
}
