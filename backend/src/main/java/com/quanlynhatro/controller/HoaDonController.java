package com.quanlynhatro.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlynhatro.dto.request.CreateHoaDonRequest;
import com.quanlynhatro.dto.request.UpdateHoaDonRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.HoaDonResponse;
import com.quanlynhatro.dto.response.InvoicePreviewResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.mapper.HoaDonMapper;
import com.quanlynhatro.service.HoaDonService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final HoaDonMapper hoaDonMapper;

    @GetMapping
    public ApiResponse<PageResponse<HoaDonResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.getPage(page, size, sortBy, direction).map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<HoaDonResponse>> search(@RequestParam(required = false) String room,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String period,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.search(room, status, period, page, size).map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<HoaDonResponse> getById(@PathVariable Long id) {
        return ApiResponse.<HoaDonResponse>builder()
                .code(200)
                .message("success")
                .result(hoaDonMapper.toHoaDonResponse(hoaDonService.getById(id)))
                .build();
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<PageResponse<HoaDonResponse>> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction).map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/hop-dong/{hopDongId}")
    public ApiResponse<PageResponse<HoaDonResponse>> getByHopDong(@PathVariable Long hopDongId,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size,
                                       @RequestParam(required = false) String sortBy,
                                       @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.getPageByHopDongId(hopDongId, page, size, sortBy, direction).map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ApiResponse<PageResponse<HoaDonResponse>> getByTrangThai(@PathVariable String trangThai,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.getPageByTrangThai(trangThai, page, size, sortBy, direction).map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/ky")
    public ApiResponse<PageResponse<HoaDonResponse>> getByKyHoaDon(@RequestParam String kyHoaDon,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.getPageByKyHoaDon(kyHoaDon, page, size, sortBy, direction).map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/exists")
    public ApiResponse<Boolean> existsByLockScope(@RequestParam(required = false) Long hopDongId,
                                                  @RequestParam(required = false) Long phongTroId,
                                                  @RequestParam String kyHoaDon) {
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("success")
                .result(hoaDonService.existsByLockScope(hopDongId, phongTroId, kyHoaDon))
                .build();
    }

    @GetMapping("/preview")
    public ApiResponse<InvoicePreviewResponse> preview(@RequestParam(required = false) Long phongTroId,
                                                       @RequestParam(required = false) Long hopDongId,
                                                       @RequestParam String kyHoaDon) {
        if (hopDongId != null) {
            return ApiResponse.<InvoicePreviewResponse>builder()
                    .code(200)
                    .message("success")
                    .result(hoaDonService.previewByContract(hopDongId, kyHoaDon))
                    .build();
        }
        if (phongTroId != null) {
            return ApiResponse.<InvoicePreviewResponse>builder()
                    .code(200)
                    .message("success")
                    .result(hoaDonService.previewByRoom(phongTroId, kyHoaDon))
                    .build();
        }
        throw new AppException(HttpStatus.BAD_REQUEST, "Vui long cung cap phongTroId hoac hopDongId");
    }

    @PostMapping
    public ApiResponse<HoaDonResponse> create(@Valid @RequestBody CreateHoaDonRequest request) {
        return ApiResponse.<HoaDonResponse>builder()
                .code(200)
                .message("success")
                .result(hoaDonMapper.toHoaDonResponse(hoaDonService.createFromRequest(request)))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<HoaDonResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateHoaDonRequest request) {
        return ApiResponse.<HoaDonResponse>builder()
                .code(200)
                .message("success")
                .result(hoaDonMapper.toHoaDonResponse(hoaDonService.update(id, toEntity(request))))
                .build();
    }

    @PutMapping("/{id}/da-thanh-toan")
    public ApiResponse<HoaDonResponse> markAsPaid(@PathVariable Long id) {
        return ApiResponse.<HoaDonResponse>builder()
                .code(200)
                .message("success")
                .result(hoaDonMapper.toHoaDonResponse(hoaDonService.markAsPaid(id)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hoaDonService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa hoa don thanh cong")
                .build();
    }

    private HoaDon toEntity(UpdateHoaDonRequest request) {
        HoaDon entity = new HoaDon();
        entity.setHopDong(toHopDong(request.getHopDongId()));
        entity.setPhongTro(toPhongTro(request.getPhongTroId()));
        entity.setNgayLap(request.getNgayLap());
        entity.setKyHoaDon(request.getKyHoaDon());
        entity.setTienPhong(request.getTienPhong());
        entity.setTienDien(request.getTienDien());
        entity.setTienNuoc(request.getTienNuoc());
        entity.setTienDichVu(request.getTienDichVu());
        entity.setTongTien(request.getTongTien());
        entity.setTrangThai(request.getTrangThai());
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

    private PhongTro toPhongTro(Long id) {
        if (id == null) {
            return null;
        }
        PhongTro phongTro = new PhongTro();
        phongTro.setPhongTroId(id);
        return phongTro;
    }

}

