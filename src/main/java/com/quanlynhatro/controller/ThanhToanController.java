package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreateThanhToanRequest;
import com.quanlynhatro.dto.request.UpdateThanhToanRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.ThanhToanResponse;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.ThanhToan;
import com.quanlynhatro.mapper.ThanhToanMapper;
import com.quanlynhatro.service.ThanhToanService;
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
@RequestMapping("/api/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController extends ApiControllerSupport {

    private final ThanhToanService thanhToanService;
    private final ThanhToanMapper thanhToanMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                thanhToanService::getAll,
                () -> thanhToanService.getPage(page, size, sortBy, direction),
                thanhToanMapper::toResponse
        );
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false) String room,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String period,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return success(thanhToanService.search(room, status, period, page, size).map(thanhToanMapper::toResponse));
    }

    @GetMapping("/summary")
    public ApiResponse<?> summary() {
        return success(thanhToanService.getPaymentSummary());
    }

    @GetMapping("/{id}")
    public ApiResponse<ThanhToanResponse> getById(@PathVariable Long id) {
        return success(thanhToanMapper.toResponse(thanhToanService.getById(id)));
    }

    @GetMapping("/hoa-don/{hoaDonId}")
    public ApiResponse<ThanhToanResponse> getByHoaDonId(@PathVariable Long hoaDonId) {
        return success(thanhToanMapper.toResponse(thanhToanService.getByHoaDonId(hoaDonId)));
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ApiResponse<?> getByTrangThai(@PathVariable String trangThai) {
        return success(thanhToanService.getByTrangThai(trangThai).stream().map(thanhToanMapper::toResponse).toList());
    }

    @PostMapping
    public ApiResponse<ThanhToanResponse> create(@Valid @RequestBody CreateThanhToanRequest request) {
        return success(thanhToanMapper.toResponse(thanhToanService.create(toEntity(request))));
    }

    @PutMapping("/{id}")
    public ApiResponse<ThanhToanResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateThanhToanRequest request) {
        return success(thanhToanMapper.toResponse(thanhToanService.update(id, toEntity(request))));
    }

    @PutMapping("/hoa-don/{hoaDonId}/xac-nhan")
    public ApiResponse<ThanhToanResponse> confirmInvoicePaid(@PathVariable Long hoaDonId) {
        return success(thanhToanMapper.toResponse(thanhToanService.confirmInvoicePaid(hoaDonId)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        thanhToanService.delete(id);
        return successMessage("Xoa thanh toan thanh cong");
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
