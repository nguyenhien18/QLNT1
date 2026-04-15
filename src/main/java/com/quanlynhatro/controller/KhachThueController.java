package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreateKhachThueRequest;
import com.quanlynhatro.dto.request.KhachThueUpdateRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.KhachThueResponse;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.mapper.KhachThueMapper;
import com.quanlynhatro.service.KhachThueService;
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

@RestController
@RequestMapping("/api/khach-thue")
@RequiredArgsConstructor
public class KhachThueController extends ApiControllerSupport {

    private final KhachThueService khachThueService;
    private final KhachThueMapper khachThueMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                khachThueService::getAll,
                () -> khachThueService.getPage(page, size, sortBy, direction),
                khachThueMapper::toResponse
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<KhachThueResponse> getById(@PathVariable Long id) {
        return success(khachThueMapper.toResponse(khachThueService.getById(id)));
    }

    @GetMapping("/email")
    public ApiResponse<KhachThueResponse> getByEmail(@RequestParam String email) {
        return success(khachThueMapper.toResponse(khachThueService.getByEmail(email)));
    }

    @GetMapping("/ten-dang-nhap")
    public ApiResponse<KhachThueResponse> getByTenDangNhap(@RequestParam String tenDangNhap) {
        return success(khachThueMapper.toResponse(khachThueService.getByTenDangNhap(tenDangNhap)));
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return success(khachThueService.search(keyword, page, size).map(khachThueMapper::toResponse));
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
        return success(khachThueMapper.toResponse(khachThueService.create(khachThue)));
    }

    @PutMapping("/{id}")
    public ApiResponse<KhachThueResponse> update(@PathVariable Long id, @Valid @RequestBody KhachThueUpdateRequest request) {
        return success(khachThueMapper.toResponse(khachThueService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        khachThueService.delete(id);
        return successMessage("Xoa khach thue thanh cong");
    }
}
