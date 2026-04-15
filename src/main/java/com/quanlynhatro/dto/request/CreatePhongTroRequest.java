package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.PhongTro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePhongTroRequest {
    @NotNull(message = "Chu tro khong duoc de trong")
    @Positive(message = "Chu tro khong hop le")
    private Long chuTroId;

    @NotBlank(message = "Ten phong khong duoc de trong")
    @Size(max = 100, message = "Ten phong toi da 100 ky tu")
    private String tenPhong;

    @Size(max = 100, message = "Loai phong toi da 100 ky tu")
    private String loaiPhong;

    @NotNull(message = "Gia thue khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Gia thue phai lon hon hoac bang 0")
    private BigDecimal giaThue;

    @NotNull(message = "Suc chua khong duoc de trong")
    @Positive(message = "Suc chua phai lon hon 0")
    private Integer sucChua;

    @Size(max = 255, message = "Mo ta toi da 255 ky tu")
    private String moTa;
    private PhongTro.TrangThai trangThai;
}
