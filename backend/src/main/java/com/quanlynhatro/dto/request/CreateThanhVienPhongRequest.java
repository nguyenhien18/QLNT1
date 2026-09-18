package com.quanlynhatro.dto.request;

import lombok.Data;

import com.quanlynhatro.entity.ThanhVienPhong;

@Data
public class CreateThanhVienPhongRequest {
    private Long hopDongId;
    private Long khachThueId;
    private String hoTen;
    private String sdt;
    private String cccd;
    private ThanhVienPhong.VaiTro vaiTro;
}
