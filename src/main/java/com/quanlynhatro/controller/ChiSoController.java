package com.quanlynhatro.controller;

import java.util.List;

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

import com.quanlynhatro.dto.request.CreateChiSoRequest;
import com.quanlynhatro.dto.request.UpdateChiSoRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.ChiSoResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.mapper.ChiSoMapper;
import com.quanlynhatro.mapper.ChiSoRequestMapper;
import com.quanlynhatro.service.ChiSoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chi-so")
@RequiredArgsConstructor
public class ChiSoController {

    private final ChiSoService chiSoService;
    private final ChiSoMapper chiSoMapper;
    private final ChiSoRequestMapper chiSoRequestMapper;

    @GetMapping
    public ApiResponse<PageResponse<ChiSoResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        chiSoService.getPage(page, size, sortBy, direction).map(chiSoMapper::toChiSoResponse)
                ))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ChiSoResponse>> search(@RequestParam(required = false) String type,
                                 @RequestParam(required = false) String room,
                                 @RequestParam(required = false) String period,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        chiSoService.search(type, room, period, page, size).map(chiSoMapper::toChiSoResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChiSoResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ChiSoResponse>builder()
                .code(200)
                .message("success")
                .result(chiSoMapper.toChiSoResponse(chiSoService.getById(id)))
                .build();
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<PageResponse<ChiSoResponse>> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        chiSoService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction).map(chiSoMapper::toChiSoResponse)
                ))
                .build();
    }

    @GetMapping("/phong/{phongTroId}/ky")
    public ApiResponse<List<ChiSoResponse>> getByPhongTroVaKy(@PathVariable Long phongTroId, @RequestParam String ky) {
        return ApiResponse.<List<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(chiSoService.getByPhongTroIdAndKy(phongTroId, ky).stream().map(chiSoMapper::toChiSoResponse).toList())
                .build();
    }

    @GetMapping("/phong/{phongTroId}/loai")
    public ApiResponse<List<ChiSoResponse>> getByPhongTroVaLoai(@PathVariable Long phongTroId, @RequestParam String loai) {
        return ApiResponse.<List<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(chiSoService.getByPhongTroIdAndLoai(phongTroId, loai).stream().map(chiSoMapper::toChiSoResponse).toList())
                .build();
    }

    @PostMapping
    public ApiResponse<ChiSoResponse> create(@Valid @RequestBody CreateChiSoRequest request) {
        return ApiResponse.<ChiSoResponse>builder()
                .code(200)
                .message("success")
                .result(chiSoMapper.toChiSoResponse(chiSoService.create(chiSoRequestMapper.toEntity(request))))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ChiSoResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateChiSoRequest request) {
        return ApiResponse.<ChiSoResponse>builder()
                .code(200)
                .message("success")
                .result(chiSoMapper.toChiSoResponse(chiSoService.update(id, chiSoRequestMapper.toEntity(request))))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        chiSoService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa chi so thanh cong")
                .build();
    }

}

