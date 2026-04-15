package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.ChiSo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateChiSoRequest {
    @Positive(message = "Phong tro khong hop le")
    private Long phongTroId;

    @Positive(message = "Hop dong khong hop le")
    private Long hopDongId;

    @NotNull(message = "Loai chi so khong duoc de trong")
    private ChiSo.Loai loai;

    @NotBlank(message = "Ky khong duoc de trong")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Ky phai co dang yyyy-MM")
    private String ky;
    private LocalDate thoiDiem;

    @NotNull(message = "Chi so cu khong duoc de trong")
    @Min(value = 0, message = "Chi so cu phai lon hon hoac bang 0")
    private Integer chiSoCu;

    @NotNull(message = "Chi so moi khong duoc de trong")
    @Min(value = 0, message = "Chi so moi phai lon hon hoac bang 0")
    private Integer chiSoMoi;

    @Min(value = 0, message = "Luong tieu thu phai lon hon hoac bang 0")
    private Integer luongTieuThu;

    @DecimalMin(value = "0.0", inclusive = true, message = "Don gia phai lon hon hoac bang 0")
    private BigDecimal donGia;

    @DecimalMin(value = "0.0", inclusive = true, message = "Thanh tien phai lon hon hoac bang 0")
    private BigDecimal thanhTien;
}
