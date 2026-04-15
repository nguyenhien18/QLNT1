package com.quanlynhatro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chi_so",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chi_so_hop_dong_ky_loai", columnNames = {"hop_dong_id", "ky", "loai"})
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiSo {

    public enum Loai { DIEN, NUOC }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chi_so_id")
    private Long chiSoId;

    @ManyToOne
    @JoinColumn(name = "phong_tro_id", nullable = false)
    private PhongTro phongTro;

    @ManyToOne
    @JoinColumn(name = "hop_dong_id")
    private HopDong hopDong;

    @NotNull(message = "Loai chi so khong duoc de trong")
    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false)
    private Loai loai;

    @NotBlank(message = "Ky khong duoc de trong")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Ky phai co dang yyyy-MM")
    @Column(name = "ky", nullable = false, length = 7)
    private String ky;

    @Column(name = "thoi_diem", nullable = false)
    private LocalDate thoiDiem = LocalDate.now();

    @NotNull(message = "Chi so cu khong duoc de trong")
    @Min(value = 0, message = "Chi so cu phai lon hon hoac bang 0")
    @Column(name = "chi_so_cu", nullable = false)
    private Integer chiSoCu;

    @NotNull(message = "Chi so moi khong duoc de trong")
    @Min(value = 0, message = "Chi so moi phai lon hon hoac bang 0")
    @Column(name = "chi_so_moi", nullable = false)
    private Integer chiSoMoi;

    @Column(name = "luong_tieu_thu", nullable = false)
    private Integer luongTieuThu;

    @DecimalMin(value = "0.0", inclusive = true, message = "Don gia phai lon hon hoac bang 0")
    @Column(name = "don_gia", precision = 12, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", precision = 12, scale = 2)
    private BigDecimal thanhTien;
}




