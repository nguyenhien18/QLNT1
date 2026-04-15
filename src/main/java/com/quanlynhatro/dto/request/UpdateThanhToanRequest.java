package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.ThanhToan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateThanhToanRequest {
    @Positive(message = "Hoa don khong hop le")
    private Long hoaDonId;

    @DecimalMin(value = "0.0", inclusive = false, message = "So tien phai lon hon 0")
    private BigDecimal soTien;
    private LocalDate ngayThanhToan;

    @Size(max = 50, message = "Phuong thuc toi da 50 ky tu")
    private String phuongThuc;

    @Size(max = 100, message = "Ma giao dich toi da 100 ky tu")
    private String maGiaoDich;

    @Size(max = 255, message = "Ghi chu toi da 255 ky tu")
    private String ghiChu;
    private ThanhToan.TrangThai trangThai;
}
