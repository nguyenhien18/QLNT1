package com.quanlynhatro.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
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

