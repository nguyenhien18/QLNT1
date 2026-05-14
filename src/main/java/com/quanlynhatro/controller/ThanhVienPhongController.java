package com.quanlynhatro.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlynhatro.dto.request.CreateThanhVienPhongRequest;
import com.quanlynhatro.dto.request.UpdateThanhVienPhongRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.dto.response.ThanhVienPhongResponse;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.entity.ThanhVienPhong;
import com.quanlynhatro.mapper.ThanhVienPhongMapper;
import com.quanlynhatro.service.ThanhVienPhongService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/thanh-vien-phong")
@RequiredArgsConstructor
public class ThanhVienPhongController {

    private final ThanhVienPhongService thanhVienPhongService;
    private final ThanhVienPhongMapper thanhVienPhongMapper;

    @GetMapping
    public ApiResponse<PageResponse<ThanhVienPhongResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<ThanhVienPhongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        thanhVienPhongService.getPage(page, size, sortBy, direction).map(thanhVienPhongMapper::toThanhVienPhongResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ThanhVienPhongResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ThanhVienPhongResponse>builder()
                .code(200)
                .message("success")
                .result(thanhVienPhongMapper.toThanhVienPhongResponse(thanhVienPhongService.getById(id)))
                .build();
    }

    @GetMapping("/hop-dong/{hopDongId}")
    public ApiResponse<PageResponse<ThanhVienPhongResponse>> getByHopDong(@PathVariable Long hopDongId,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size,
                                       @RequestParam(required = false) String sortBy,
                                       @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<ThanhVienPhongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        thanhVienPhongService.getPageByHopDongId(hopDongId, page, size, sortBy, direction)
                                .map(thanhVienPhongMapper::toThanhVienPhongResponse)
                ))
                .build();
    }

    @PostMapping
    public ApiResponse<ThanhVienPhongResponse> create(@Valid @RequestBody CreateThanhVienPhongRequest request) {
        return ApiResponse.<ThanhVienPhongResponse>builder()
                .code(200)
                .message("success")
                .result(thanhVienPhongMapper.toThanhVienPhongResponse(thanhVienPhongService.create(toEntity(request))))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ThanhVienPhongResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateThanhVienPhongRequest request) {
        return ApiResponse.<ThanhVienPhongResponse>builder()
                .code(200)
                .message("success")
                .result(thanhVienPhongMapper.toThanhVienPhongResponse(thanhVienPhongService.update(id, toEntity(request))))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        thanhVienPhongService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa thanh vien phong thanh cong")
                .build();
    }

    private ThanhVienPhong toEntity(CreateThanhVienPhongRequest request) {
        ThanhVienPhong entity = new ThanhVienPhong();
        entity.setHopDong(toHopDong(request.getHopDongId()));
        entity.setKhachThue(toKhachThue(request.getKhachThueId()));
        entity.setHoTen(request.getHoTen());
        entity.setSdt(request.getSdt());
        entity.setCccd(request.getCccd());
        entity.setVaiTro(request.getVaiTro());
        return entity;
    }

    private ThanhVienPhong toEntity(UpdateThanhVienPhongRequest request) {
        ThanhVienPhong entity = new ThanhVienPhong();
        entity.setHopDong(toHopDong(request.getHopDongId()));
        entity.setKhachThue(toKhachThue(request.getKhachThueId()));
        entity.setHoTen(request.getHoTen());
        entity.setSdt(request.getSdt());
        entity.setCccd(request.getCccd());
        entity.setVaiTro(request.getVaiTro());
        return entity;
    }

    private HopDong toHopDong(Long id) {
        if (id == null) {
            return null;
        }
        HopDong hopDong = new HopDong();
        hopDong.setHopDongId(id);
        return hopDong;
    }

    private KhachThue toKhachThue(Long id) {
        if (id == null) {
            return null;
        }
        KhachThue khachThue = new KhachThue();
        khachThue.setKhachThueId(id);
        return khachThue;
    }

}

