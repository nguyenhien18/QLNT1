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

import com.quanlynhatro.dto.request.CreateKhachThueRequest;
import com.quanlynhatro.dto.request.KhachThueUpdateRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.KhachThueResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.mapper.KhachThueMapper;
import com.quanlynhatro.service.KhachThueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/khach-thue")
@RequiredArgsConstructor
public class KhachThueController {

    private final KhachThueService khachThueService;
    private final KhachThueMapper khachThueMapper;

    @GetMapping
    public ApiResponse<PageResponse<KhachThueResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<KhachThueResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        khachThueService.getPage(page, size, sortBy, direction).map(khachThueMapper::toKhachThueResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<KhachThueResponse> getById(@PathVariable Long id) {
        return ApiResponse.<KhachThueResponse>builder()
                .code(200)
                .message("success")
                .result(khachThueMapper.toKhachThueResponse(khachThueService.getById(id)))
                .build();
    }

    @GetMapping("/email")
    public ApiResponse<KhachThueResponse> getByEmail(@RequestParam String email) {
        return ApiResponse.<KhachThueResponse>builder()
                .code(200)
                .message("success")
                .result(khachThueMapper.toKhachThueResponse(khachThueService.getByEmail(email)))
                .build();
    }

    @GetMapping("/ten-dang-nhap")
    public ApiResponse<KhachThueResponse> getByTenDangNhap(@RequestParam String tenDangNhap) {
        return ApiResponse.<KhachThueResponse>builder()
                .code(200)
                .message("success")
                .result(khachThueMapper.toKhachThueResponse(khachThueService.getByTenDangNhap(tenDangNhap)))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<KhachThueResponse>> search(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<KhachThueResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        khachThueService.search(keyword, page, size).map(khachThueMapper::toKhachThueResponse)
                ))
                .build();
    }

    @PostMapping
    public ApiResponse<KhachThueResponse> create(@Valid @RequestBody CreateKhachThueRequest request) {
        KhachThue khachThue = new KhachThue();
        khachThue.setHoTen(request.getHoTen());
        khachThue.setCccd(request.getCccd());
        khachThue.setSdt(request.getSdt());
        khachThue.setEmail(request.getEmail());
        khachThue.setNgaySinh(request.getNgaySinh());
        khachThue.setGioiTinh(request.getGioiTinh());
        khachThue.setDiaChi(request.getDiaChi());
        khachThue.setTenDangNhap(request.getTenDangNhap());
        khachThue.setMatKhau(request.getMatKhau());
        khachThue.setTrangThai(request.getTrangThai());
        return ApiResponse.<KhachThueResponse>builder()
                .code(200)
                .message("success")
                .result(khachThueMapper.toKhachThueResponse(khachThueService.create(khachThue)))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<KhachThueResponse> update(@PathVariable Long id, @Valid @RequestBody KhachThueUpdateRequest request) {
        return ApiResponse.<KhachThueResponse>builder()
                .code(200)
                .message("success")
                .result(khachThueMapper.toKhachThueResponse(khachThueService.update(id, request)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        khachThueService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa khach thue thanh cong")
                .build();
    }

}

