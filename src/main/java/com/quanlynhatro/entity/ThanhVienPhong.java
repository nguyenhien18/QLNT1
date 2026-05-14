package com.quanlynhatro.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "thanh_vien_phong")
public class ThanhVienPhong {

    public enum VaiTro {
        DAI_DIEN,
        O_CUNG
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thanh_vien_id")
    private Long thanhVienId;

    @ManyToOne
    @JoinColumn(name = "hop_dong_id", nullable = false)
    private HopDong hopDong;

    @ManyToOne
    @JoinColumn(name = "khach_thue_id")
    private KhachThue khachThue;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "cccd", length = 20)
    private String cccd;

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private VaiTro vaiTro = VaiTro.O_CUNG;
}
