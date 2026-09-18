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

import com.quanlynhatro.dto.request.CreateDichVuRequest;
import com.quanlynhatro.dto.request.UpdateDichVuRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.DichVuResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.entity.DichVu;
import com.quanlynhatro.mapper.DichVuMapper;
import com.quanlynhatro.service.DichVuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dich-vu")
@RequiredArgsConstructor
public class DichVuController {

    private final DichVuService dichVuService;
    private final DichVuMapper dichVuMapper;

    @GetMapping
    public ApiResponse<PageResponse<DichVuResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<DichVuResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        dichVuService.getPage(page, size, sortBy, direction).map(dichVuMapper::toDichVuResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DichVuResponse> getById(@PathVariable Long id) {
        return ApiResponse.<DichVuResponse>builder()
                .code(200)
                .message("success")
                .result(dichVuMapper.toDichVuResponse(dichVuService.getById(id)))
                .build();
    }

    @PostMapping
    public ApiResponse<DichVuResponse> create(@Valid @RequestBody CreateDichVuRequest request) {
        DichVu dichVu = new DichVu();
        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setGiaDichVu(request.getGiaDichVu());
        dichVu.setDonViTinh(request.getDonViTinh());
        return ApiResponse.<DichVuResponse>builder()
                .code(200)
                .message("success")
                .result(dichVuMapper.toDichVuResponse(dichVuService.create(dichVu)))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DichVuResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateDichVuRequest request) {
        DichVu dichVu = new DichVu();
        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setGiaDichVu(request.getGiaDichVu());
        dichVu.setDonViTinh(request.getDonViTinh());
        return ApiResponse.<DichVuResponse>builder()
                .code(200)
                .message("success")
                .result(dichVuMapper.toDichVuResponse(dichVuService.update(id, dichVu)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dichVuService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa dich vu thanh cong")
                .build();
    }

}

