package com.quanlynhatro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hop_dong")
public class HopDong {

    public enum TrangThai {
        CON_HIEU_LUC,
        HET_HIEU_LUC,
        HUY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hop_dong_id")
    private Long hopDongId;

    @ManyToOne
    @JoinColumn(name = "phong_tro_id", nullable = false)
    private PhongTro phongTro;

    @ManyToOne
    @JoinColumn(name = "khach_thue_id", nullable = false)
    private KhachThue khachThue;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "tien_coc", precision = 12, scale = 2)
    private BigDecimal tienCoc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThai trangThai = TrangThai.CON_HIEU_LUC;
}
