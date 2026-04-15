package com.quanlynhatro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "thanh_toan")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThanhToan {

    public enum TrangThai { THANH_CONG, THAT_BAI }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thanh_toan_id")
    private Long thanhToanId;

    @OneToOne
    @JoinColumn(name = "hoa_don_id", nullable = false, unique = true)
    private HoaDon hoaDon;

    @NotNull(message = "So tien khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = false, message = "So tien phai lon hon 0")
    @Column(name = "so_tien", nullable = false, precision = 12, scale = 2)
    private BigDecimal soTien;

    @NotNull(message = "Ngay thanh toan khong duoc de trong")
    @Column(name = "ngay_thanh_toan", nullable = false)
    private LocalDate ngayThanhToan;

    @Column(name = "phuong_thuc", length = 50)
    private String phuongThuc;

    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @NotNull(message = "Trang thai thanh toan khong duoc de trong")
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThai trangThai;
}




