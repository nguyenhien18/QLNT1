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

import com.quanlynhatro.dto.request.CreateThanhToanRequest;
import com.quanlynhatro.dto.request.UpdateThanhToanRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.dto.response.PaymentListItemResponse;
import com.quanlynhatro.dto.response.ThanhToanResponse;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.ThanhToan;
import com.quanlynhatro.mapper.ThanhToanMapper;
import com.quanlynhatro.service.ThanhToanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController {

    private final ThanhToanService thanhToanService;
    private final ThanhToanMapper thanhToanMapper;

    @GetMapping
    public ApiResponse<PageResponse<ThanhToanResponse>> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return ApiResponse.<PageResponse<ThanhToanResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        thanhToanService.getPage(page, size, sortBy, direction).map(thanhToanMapper::toThanhToanResponse)
                ))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ThanhToanResponse>> search(@RequestParam(required = false) String room,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String period,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return ApiResponse.<PageResponse<ThanhToanResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        thanhToanService.search(room, status, period, page, size).map(thanhToanMapper::toThanhToanResponse)
                ))
                .build();
    }

    @GetMapping("/summary")
    public ApiResponse<List<PaymentListItemResponse>> summary() {
        return ApiResponse.<List<PaymentListItemResponse>>builder()
                .code(200)
                .message("success")
                .result(thanhToanService.getPaymentSummary())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ThanhToanResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ThanhToanResponse>builder()
                .code(200)
                .message("success")
                .result(thanhToanMapper.toThanhToanResponse(thanhToanService.getById(id)))
                .build();
    }

    @GetMapping("/hoa-don/{hoaDonId}")
    public ApiResponse<ThanhToanResponse> getByHoaDonId(@PathVariable Long hoaDonId) {
        return ApiResponse.<ThanhToanResponse>builder()
                .code(200)
                .message("success")
                .result(thanhToanMapper.toThanhToanResponse(thanhToanService.getByHoaDonId(hoaDonId)))
                .build();
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ApiResponse<List<ThanhToanResponse>> getByTrangThai(@PathVariable String trangThai) {
        return ApiResponse.<List<ThanhToanResponse>>builder()
                .code(200)
                .message("success")
                .result(thanhToanService.getByTrangThai(trangThai).stream().map(thanhToanMapper::toThanhToanResponse).toList())
                .build();
    }

    @PostMapping
    public ApiResponse<ThanhToanResponse> create(@Valid @RequestBody CreateThanhToanRequest request) {
        return ApiResponse.<ThanhToanResponse>builder()
                .code(200)
                .message("success")
                .result(thanhToanMapper.toThanhToanResponse(thanhToanService.create(toEntity(request))))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ThanhToanResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateThanhToanRequest request) {
        return ApiResponse.<ThanhToanResponse>builder()
                .code(200)
                .message("success")
                .result(thanhToanMapper.toThanhToanResponse(thanhToanService.update(id, toEntity(request))))
                .build();
    }

    @PutMapping("/hoa-don/{hoaDonId}/xac-nhan")
    public ApiResponse<ThanhToanResponse> confirmInvoicePaid(@PathVariable Long hoaDonId) {
        return ApiResponse.<ThanhToanResponse>builder()
                .code(200)
                .message("success")
                .result(thanhToanMapper.toThanhToanResponse(thanhToanService.confirmInvoicePaid(hoaDonId)))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        thanhToanService.delete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xoa thanh toan thanh cong")
                .build();
    }

    private ThanhToan toEntity(CreateThanhToanRequest request) {
        ThanhToan entity = new ThanhToan();
        entity.setHoaDon(toHoaDon(request.getHoaDonId()));
        entity.setSoTien(request.getSoTien());
        entity.setNgayThanhToan(request.getNgayThanhToan());
        entity.setPhuongThuc(request.getPhuongThuc());
        entity.setMaGiaoDich(request.getMaGiaoDich());
        entity.setGhiChu(request.getGhiChu());
        entity.setTrangThai(request.getTrangThai());
        return entity;
    }

    private ThanhToan toEntity(UpdateThanhToanRequest request) {
        ThanhToan entity = new ThanhToan();
        entity.setHoaDon(toHoaDon(request.getHoaDonId()));
        entity.setSoTien(request.getSoTien());
        entity.setNgayThanhToan(request.getNgayThanhToan());
        entity.setPhuongThuc(request.getPhuongThuc());
        entity.setMaGiaoDich(request.getMaGiaoDich());
        entity.setGhiChu(request.getGhiChu());
        entity.setTrangThai(request.getTrangThai());
        return entity;
    }

    private HoaDon toHoaDon(Long id) {
        if (id == null) {
            return null;
        }
        HoaDon hoaDon = new HoaDon();
        hoaDon.setHoaDonId(id);
        return hoaDon;
    }

}

