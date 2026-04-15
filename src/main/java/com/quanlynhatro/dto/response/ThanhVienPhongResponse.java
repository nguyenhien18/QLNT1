package com.quanlynhatro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThanhVienPhongResponse {
    private Long thanhVienId;
    private Long hopDongId;
    private Long khachThueId;
    private String hoTen;
    private String sdt;
    private String cccd;
    private String vaiTro;
}
