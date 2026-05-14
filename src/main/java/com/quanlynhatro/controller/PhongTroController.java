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

import com.quanlynhatro.dto.request.CreatePhongTroRequest;
import com.quanlynhatro.dto.request.UpdatePhongTroRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.dto.response.PhongTroResponse;
import com.quanlynhatro.dto.response.RoomSummaryResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.mapper.PhongTroMapper;
import com.quanlynhatro.service.PhongTroService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/phong-tro")
@RequiredArgsConstructor
public class PhongTroController {

    private final PhongTroService phongTroService;
    private final PhongTroMapper phongTroMapper;

    @GetMapping
    public ApiResponse<PageResponse<PhongTroResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<PhongTroResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongTroService.getPage(page, size, sortBy, direction).map(phongTroMapper::toPhongTroResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PhongTroResponse> getById(@PathVariable Long id) {
        return ApiResponse.<PhongTroResponse>builder()
                .code(200)
                .message("success")
                .result(phongTroMapper.toPhongTroResponse(phongTroService.getById(id)))
                .build();
    }

    @GetMapping("/summary")
    public ApiResponse<List<RoomSummaryResponse>> getSummary() {
        return ApiResponse.<List<RoomSummaryResponse>>builder()
                .code(200)
                .message("success")
                .result(phongTroService.getRoomSummaries())
                .build();
    }

    @GetMapping("/chu-tro/{chuTroId}")
    public ApiResponse<PageResponse<PhongTroResponse>> getByChuTro(@PathVariable Long chuTroId,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<PhongTroResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongTroService.getPageByChuTroId(chuTroId, page, size, sortBy, direction).map(phongTroMapper::toPhongTroResponse)
                ))
                .build();
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ApiResponse<PageResponse<PhongTroResponse>> getByTrangThai(@PathVariable String trangThai,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<PhongTroResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongTroService.getPageByTrangThai(trangThai, page, size, sortBy, direction).map(phongTroMapper::toPhongTroResponse)
                ))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<PhongTroResponse>> search(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<PhongTroResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        phongTroService.search(name, type, status, keyword, page, size).map(phongTroMapper::toPhongTroResponse)
                ))
                .build();
    }

    @PostMapping
    public ApiResponse<PhongTroResponse> create(@Valid @RequestBody CreatePhongTroRequest request) {
        return ApiResponse.<PhongTroResponse>builder()
                .code(200)
                .message("success")
                .result(phongTroMapper.toPhongTroResponse(phongTroService.create(toEntity(request))))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PhongTroResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePhongTroRequest request) {
        return ApiResponse.<PhongTroResponse>builder()
                .code(200)
                .message("success")
                .result(phongTroMapper.toPhongTroResponse(phongTroService.update(id, toEntity(request))))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        phongTroService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa phong tro thanh cong")
                .build();
    }

    private PhongTro toEntity(CreatePhongTroRequest request) {
        PhongTro phongTro = new PhongTro();
        phongTro.setChuTro(toChuTro(request.getChuTroId()));
        phongTro.setTenPhong(request.getTenPhong());
        phongTro.setLoaiPhong(request.getLoaiPhong());
        phongTro.setGiaThue(request.getGiaThue());
        phongTro.setSucChua(request.getSucChua());
        phongTro.setMoTa(request.getMoTa());
        phongTro.setTrangThai(request.getTrangThai());
        return phongTro;
    }

    private PhongTro toEntity(UpdatePhongTroRequest request) {
        PhongTro phongTro = new PhongTro();
        phongTro.setChuTro(toChuTro(request.getChuTroId()));
        phongTro.setTenPhong(request.getTenPhong());
        phongTro.setLoaiPhong(request.getLoaiPhong());
        phongTro.setGiaThue(request.getGiaThue());
        phongTro.setSucChua(request.getSucChua());
        phongTro.setMoTa(request.getMoTa());
        phongTro.setTrangThai(request.getTrangThai());
        return phongTro;
    }

    private ChuTro toChuTro(Long chuTroId) {
        if (chuTroId == null) {
            return null;
        }
        ChuTro chuTro = new ChuTro();
        chuTro.setChuTroId(chuTroId);
        return chuTro;
    }
}

