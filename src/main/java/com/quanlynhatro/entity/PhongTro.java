package com.quanlynhatro.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phong_tro")
public class PhongTro {

    public enum TrangThai {
        TRONG,
        DA_CHO_THUE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phong_tro_id")
    private Long phongTroId;

    @ManyToOne
    @JoinColumn(name = "chu_tro_id", nullable = false)
    private ChuTro chuTro;

    @Column(name = "ten_phong", nullable = false, length = 100)
    private String tenPhong;

    @Column(name = "loai_phong", length = 100)
    private String loaiPhong;

    @Column(name = "gia_thue", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaThue;

    @Column(name = "suc_chua")
    private Integer sucChua;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThai trangThai = TrangThai.TRONG;
}
