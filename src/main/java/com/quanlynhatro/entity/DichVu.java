package com.quanlynhatro.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dich_vu")
public class DichVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dich_vu_id")
    private Long dichVuId;

    @Column(name = "ten_dich_vu", nullable = false, unique = true, length = 100)
    private String tenDichVu;

    @Column(name = "gia_dich_vu", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaDichVu;

    @Column(name = "don_vi_tinh", length = 50)
    private String donViTinh;
}
