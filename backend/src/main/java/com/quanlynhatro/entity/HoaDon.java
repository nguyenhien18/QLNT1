package com.quanlynhatro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "hoa_don",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hoa_don_hop_dong_ky", columnNames = {"hop_dong_id", "ky_hoa_don"})
        }
)
public class HoaDon {

    public enum TrangThai { CHUA_THANH_TOAN, DA_THANH_TOAN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hoa_don_id")
    private Long hoaDonId;

    @ManyToOne
    @JoinColumn(name = "hop_dong_id", nullable = false)
    private HopDong hopDong;

    @ManyToOne
    @JoinColumn(name = "phong_tro_id", nullable = false)
    private PhongTro phongTro;

    @NotNull(message = "Ngay lap khong duoc de trong")
    @Column(name = "ngay_lap", nullable = false)
    private LocalDate ngayLap;

    @NotBlank(message = "Ky hoa don khong duoc de trong")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Ky hoa don phai co dang yyyy-MM")
    @Column(name = "ky_hoa_don", nullable = false, length = 7)
    private String kyHoaDon;

    @NotNull(message = "Tien phong khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tien phong phai lon hon hoac bang 0")
    @Column(name = "tien_phong", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienPhong;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tien dien phai lon hon hoac bang 0")
    @Column(name = "tien_dien", precision = 12, scale = 2)
    private BigDecimal tienDien = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tien nuoc phai lon hon hoac bang 0")
    @Column(name = "tien_nuoc", precision = 12, scale = 2)
    private BigDecimal tienNuoc = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tien dich vu phai lon hon hoac bang 0")
    @Column(name = "tien_dich_vu", precision = 12, scale = 2)
    private BigDecimal tienDichVu = BigDecimal.ZERO;

    @NotNull(message = "Tong tien khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tong tien phai lon hon hoac bang 0")
    @Column(name = "tong_tien", nullable = false, precision = 12, scale = 2)
    private BigDecimal tongTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThai trangThai = TrangThai.CHUA_THANH_TOAN;
}
