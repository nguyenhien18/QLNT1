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

import com.quanlynhatro.dto.request.ChuTroUpdateRequest;
import com.quanlynhatro.dto.request.CreateChuTroRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.ChuTroResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.mapper.ChuTroMapper;
import com.quanlynhatro.service.ChuTroService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chu-tro")
@RequiredArgsConstructor
public class ChuTroController {

    private final ChuTroService chuTroService;
    private final ChuTroMapper chuTroMapper;

    @GetMapping
    public ApiResponse<PageResponse<ChuTroResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<ChuTroResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        chuTroService.getPage(page, size, sortBy, direction).map(chuTroMapper::toChuTroResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChuTroResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ChuTroResponse>builder()
                .code(200)
                .message("success")
                .result(chuTroMapper.toChuTroResponse(chuTroService.getById(id)))
                .build();
    }

    @GetMapping("/email")
    public ApiResponse<ChuTroResponse> getByEmail(@RequestParam String email) {
        return ApiResponse.<ChuTroResponse>builder()
                .code(200)
                .message("success")
                .result(chuTroMapper.toChuTroResponse(chuTroService.getByEmail(email)))
                .build();
    }

    @PostMapping
    public ApiResponse<ChuTroResponse> create(@Valid @RequestBody CreateChuTroRequest request) {
        ChuTro chuTro = new ChuTro();
        chuTro.setHoTen(request.getHoTen());
        chuTro.setEmail(request.getEmail());
        chuTro.setSdt(request.getSdt());
        chuTro.setMatKhau(request.getMatKhau());
        return ApiResponse.<ChuTroResponse>builder()
                .code(200)
                .message("success")
                .result(chuTroMapper.toChuTroResponse(chuTroService.create(chuTro)))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ChuTroResponse> update(@PathVariable Long id, @Valid @RequestBody ChuTroUpdateRequest request) {
        return ApiResponse.<ChuTroResponse>builder()
                .code(200)
                .message("success")
                .result(chuTroMapper.toChuTroResponse(chuTroService.update(id, request)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        chuTroService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa chu tro thanh cong")
                .build();
    }

}

