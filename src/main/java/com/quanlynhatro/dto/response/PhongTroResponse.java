package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhongTroResponse {
    private Long phongTroId;
    private Long chuTroId;
    private String chuTroHoTen;
    private String tenPhong;
    private String loaiPhong;
    private BigDecimal giaThue;
    private Integer sucChua;
    private String moTa;
    private String trangThai;
}
