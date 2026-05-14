package com.quanlynhatro.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.quanlynhatro.entity.ThanhToan;

@Data
public class CreateThanhToanRequest {
    @NotNull(message = "Hoa don khong duoc de trong")
    @Positive(message = "Hoa don khong hop le")
    private Long hoaDonId;

    @NotNull(message = "So tien khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = false, message = "So tien phai lon hon 0")
    private BigDecimal soTien;

    @NotNull(message = "Ngay thanh toan khong duoc de trong")
    private LocalDate ngayThanhToan;

    @Size(max = 50, message = "Phuong thuc toi da 50 ky tu")
    private String phuongThuc;

    @Size(max = 100, message = "Ma giao dich toi da 100 ky tu")
    private String maGiaoDich;

    @Size(max = 255, message = "Ghi chu toi da 255 ky tu")
    private String ghiChu;

    @NotNull(message = "Trang thai thanh toan khong duoc de trong")
    private ThanhToan.TrangThai trangThai;
}
