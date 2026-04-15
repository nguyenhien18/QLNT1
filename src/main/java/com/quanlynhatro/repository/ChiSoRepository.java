package com.quanlynhatro.repository;

import org.springframework.stereotype.Repository;

import com.quanlynhatro.entity.ChiSo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ChiSoRepository extends JpaRepository<ChiSo, Long>, JpaSpecificationExecutor<ChiSo> {
    List<ChiSo> findByPhongTro_PhongTroId(Long phongTroId);
    List<ChiSo> findByPhongTro_PhongTroIdAndKy(Long phongTroId, String ky);
    List<ChiSo> findByPhongTro_PhongTroIdAndLoai(Long phongTroId, ChiSo.Loai loai);
    Optional<ChiSo> findByPhongTro_PhongTroIdAndKyAndLoai(Long phongTroId, String ky, ChiSo.Loai loai);
    List<ChiSo> findByPhongTro_PhongTroIdAndKyAndLoaiOrderByThoiDiemDesc(Long phongTroId, String ky, ChiSo.Loai loai);
    Optional<ChiSo> findByHopDong_HopDongIdAndKyAndLoai(Long hopDongId, String ky, ChiSo.Loai loai);
    List<ChiSo> findByHopDong_HopDongIdAndKyAndLoaiOrderByThoiDiemDesc(Long hopDongId, String ky, ChiSo.Loai loai);
    Page<ChiSo> findAll(Pageable pageable);
    Page<ChiSo> findByPhongTro_PhongTroId(Long phongTroId, Pageable pageable);
    Page<ChiSo> findByPhongTro_PhongTroIdAndLoai(Long phongTroId, ChiSo.Loai loai, Pageable pageable);
    Page<ChiSo> findByPhongTro_PhongTroIdAndKyContaining(Long phongTroId, String ky, Pageable pageable);
    Page<ChiSo> findByPhongTro_PhongTroIdIn(List<Long> phongTroIds, Pageable pageable);

    @Query(value = """
            select cs
            from ChiSo cs
            where (:type is null or cs.loai = :type)
              and (:room = '' or lower(coalesce(cs.phongTro.tenPhong, '')) like concat('%', :room, '%'))
              and (:period = '' or lower(coalesce(cs.ky, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(cs)
            from ChiSo cs
            where (:type is null or cs.loai = :type)
              and (:room = '' or lower(coalesce(cs.phongTro.tenPhong, '')) like concat('%', :room, '%'))
              and (:period = '' or lower(coalesce(cs.ky, '')) like concat('%', :period, '%'))
            """)
    Page<ChiSo> search(@Param("type") ChiSo.Loai type,
                       @Param("room") String room,
                       @Param("period") String period,
                       Pageable pageable);

    @Query(value = """
            select cs
            from ChiSo cs
            where cs.phongTro.phongTroId in :roomIds
              and (:type is null or cs.loai = :type)
              and (:period = '' or lower(coalesce(cs.ky, '')) like concat('%', :period, '%'))
            """,
            countQuery = """
            select count(cs)
            from ChiSo cs
            where cs.phongTro.phongTroId in :roomIds
              and (:type is null or cs.loai = :type)
              and (:period = '' or lower(coalesce(cs.ky, '')) like concat('%', :period, '%'))
            """)
    Page<ChiSo> searchByRoomIds(@Param("roomIds") List<Long> roomIds,
                                @Param("type") ChiSo.Loai type,
                                @Param("period") String period,
                                Pageable pageable);
}


