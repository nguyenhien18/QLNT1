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

import com.quanlynhatro.dto.request.CreateHopDongRequest;
import com.quanlynhatro.dto.request.UpdateHopDongRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.HopDongResponse;
import com.quanlynhatro.dto.response.KhachThueResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.dto.response.PhongTroResponse;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.mapper.HopDongMapper;
import com.quanlynhatro.mapper.KhachThueMapper;
import com.quanlynhatro.mapper.PhongTroMapper;
import com.quanlynhatro.service.HopDongService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hop-dong")
@RequiredArgsConstructor
public class HopDongController {

    private final HopDongService hopDongService;
    private final HopDongMapper hopDongMapper;
    private final PhongTroMapper phongTroMapper;
    private final KhachThueMapper khachThueMapper;

    @GetMapping
    public ApiResponse<PageResponse<HopDongResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HopDongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hopDongService.getPage(page, size, sortBy, direction).map(hopDongMapper::toHopDongResponse)
                ))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<HopDongResponse>> search(@RequestParam(required = false) String room,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HopDongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hopDongService.search(room, status, page, size).map(hopDongMapper::toHopDongResponse)
                ))
                .build();
    }

    @GetMapping("/phong-trong")
    public ApiResponse<List<PhongTroResponse>> getAvailableRooms() {
        List<PhongTro> rooms = hopDongService.getAvailableRooms();
        return ApiResponse.<List<PhongTroResponse>>builder()
                .code(200)
                .message("success")
                .result(rooms.stream().map(phongTroMapper::toPhongTroResponse).toList())
                .build();
    }

    @GetMapping("/khach-thue-trong")
    public ApiResponse<List<KhachThueResponse>> getAvailableTenants(@RequestParam(required = false) Long hopDongId) {
        List<KhachThue> tenants = hopDongService.getAvailableTenants(hopDongId);
        return ApiResponse.<List<KhachThueResponse>>builder()
                .code(200)
                .message("success")
                .result(tenants.stream().map(khachThueMapper::toKhachThueResponse).toList())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<HopDongResponse> getById(@PathVariable Long id) {
        return ApiResponse.<HopDongResponse>builder()
                .code(200)
                .message("success")
                .result(hopDongMapper.toHopDongResponse(hopDongService.getById(id)))
                .build();
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<PageResponse<HopDongResponse>> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HopDongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hopDongService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction).map(hopDongMapper::toHopDongResponse)
                ))
                .build();
    }

    @GetMapping("/khach/{khachThueId}")
    public ApiResponse<PageResponse<HopDongResponse>> getByKhachThue(@PathVariable Long khachThueId,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HopDongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hopDongService.getPageByKhachThueId(khachThueId, page, size, sortBy, direction).map(hopDongMapper::toHopDongResponse)
                ))
                .build();
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ApiResponse<PageResponse<HopDongResponse>> getByTrangThai(@PathVariable String trangThai,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HopDongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hopDongService.getPageByTrangThai(trangThai, page, size, sortBy, direction).map(hopDongMapper::toHopDongResponse)
                ))
                .build();
    }

    @PostMapping
    public ApiResponse<HopDongResponse> create(@Valid @RequestBody CreateHopDongRequest request) {
        return ApiResponse.<HopDongResponse>builder()
                .code(200)
                .message("success")
                .result(hopDongMapper.toHopDongResponse(hopDongService.createFromRequest(request)))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<HopDongResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateHopDongRequest request) {
        return ApiResponse.<HopDongResponse>builder()
                .code(200)
                .message("success")
                .result(hopDongMapper.toHopDongResponse(hopDongService.update(id, request)))
                .build();
    }

    @PutMapping("/{id}/ket-thuc")
    public ApiResponse<HopDongResponse> ketThucHopDong(@PathVariable Long id) {
        return ApiResponse.<HopDongResponse>builder()
                .code(200)
                .message("success")
                .result(hopDongMapper.toHopDongResponse(hopDongService.ketThucHopDong(id)))
                .build();
    }

    @PutMapping("/{id}/huy")
    public ApiResponse<HopDongResponse> huyHopDong(@PathVariable Long id) {
        return ApiResponse.<HopDongResponse>builder()
                .code(200)
                .message("success")
                .result(hopDongMapper.toHopDongResponse(hopDongService.huyHopDong(id)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hopDongService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa hop dong thanh cong")
                .build();
    }

}

