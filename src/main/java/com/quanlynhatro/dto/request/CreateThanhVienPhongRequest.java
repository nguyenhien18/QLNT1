package com.quanlynhatro.dto.request;

import com.quanlynhatro.entity.ThanhVienPhong;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateThanhVienPhongRequest {
    private Long hopDongId;
    private Long khachThueId;
    private String hoTen;
    private String sdt;
    private String cccd;
    private ThanhVienPhong.VaiTro vaiTro;
}
