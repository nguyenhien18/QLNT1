package com.quanlynhatro.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlynhatro.dto.request.PhongDichVuRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.dto.response.PhongDichVuResponse;
import com.quanlynhatro.mapper.PhongDichVuMapper;
import com.quanlynhatro.service.PhongDichVuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/phong-dich-vu")
@RequiredArgsConstructor
public class PhongDichVuController {

    private final PhongDichVuService phongDichVuService;
    private final PhongDichVuMapper phongDichVuMapper;

    @GetMapping
    public ApiResponse<PageResponse<PhongDichVuResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<PhongDichVuResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongDichVuService.getPage(page, size, sortBy, direction).map(phongDichVuMapper::toPhongDichVuResponse)
                ))
                .build();
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<PageResponse<PhongDichVuResponse>> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<PhongDichVuResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongDichVuService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction)
                                .map(phongDichVuMapper::toPhongDichVuResponse)
                ))
                .build();
    }

    @GetMapping("/dich-vu/{dichVuId}")
    public ApiResponse<PageResponse<PhongDichVuResponse>> getByDichVu(@PathVariable Long dichVuId,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<PhongDichVuResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongDichVuService.getPageByDichVuId(dichVuId, page, size, sortBy, direction)
                                .map(phongDichVuMapper::toPhongDichVuResponse)
                ))
                .build();
    }

    @PostMapping
    public ApiResponse<PhongDichVuResponse> create(@Valid @RequestBody PhongDichVuRequest request) {
        return ApiResponse.<PhongDichVuResponse>builder()
                .code(200)
                .message("success")
                .result(phongDichVuMapper.toPhongDichVuResponse(phongDichVuService.create(request)))
                .build();
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam Long phongTroId, @RequestParam Long dichVuId) {
        phongDichVuService.delete(phongTroId, dichVuId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa dich vu khoi phong thanh cong")
                .build();
    }

}

