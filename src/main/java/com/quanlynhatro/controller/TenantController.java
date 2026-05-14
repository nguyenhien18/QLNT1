package com.quanlynhatro.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.ChiSoResponse;
import com.quanlynhatro.dto.response.HoaDonResponse;
import com.quanlynhatro.dto.response.HopDongResponse;
import com.quanlynhatro.dto.response.PageResponse;
import com.quanlynhatro.dto.response.TenantProfileResponse;
import com.quanlynhatro.dto.response.ThanhToanResponse;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.mapper.ChiSoMapper;
import com.quanlynhatro.mapper.HoaDonMapper;
import com.quanlynhatro.mapper.HopDongMapper;
import com.quanlynhatro.mapper.ThanhToanMapper;
import com.quanlynhatro.service.ChiSoService;
import com.quanlynhatro.service.CurrentUserService;
import com.quanlynhatro.service.HoaDonService;
import com.quanlynhatro.service.HopDongService;
import com.quanlynhatro.service.ThanhToanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final CurrentUserService currentUserService;
    private final HopDongService hopDongService;
    private final HoaDonService hoaDonService;
    private final ThanhToanService thanhToanService;
    private final ChiSoService chiSoService;
    private final HopDongMapper hopDongMapper;
    private final HoaDonMapper hoaDonMapper;
    private final ThanhToanMapper thanhToanMapper;
    private final ChiSoMapper chiSoMapper;

    @GetMapping("/profile")
    public ApiResponse<TenantProfileResponse> getProfile() {
        return ApiResponse.<TenantProfileResponse>builder()
                .code(200)
                .message("success")
                .result(TenantProfileResponse.from(currentUserService.getCurrentTenant()))
                .build();
    }

    @GetMapping("/hop-dong")
    public ApiResponse<PageResponse<HopDongResponse>> getContracts(@RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String sortBy,
                                       @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return ApiResponse.<PageResponse<HopDongResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hopDongService.getTenantContracts(tenant.getKhachThueId(), page, size, sortBy, direction)
                                .map(hopDongMapper::toHopDongResponse)
                ))
                .build();
    }

    @GetMapping("/hop-dong/current")
    public ApiResponse<HopDongResponse> getCurrentContract() {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return ApiResponse.<HopDongResponse>builder()
                .code(200)
                .message("success")
                .result(Optional.ofNullable(hopDongService.getCurrentTenantContract(tenant.getKhachThueId()))
                        .map(hopDongMapper::toHopDongResponse)
                        .orElse(null))
                .build();
    }

    @GetMapping("/hoa-don")
    public ApiResponse<PageResponse<HoaDonResponse>> getInvoices(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.getTenantInvoices(tenant.getKhachThueId(), page, size, sortBy, direction)
                                .map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/hoa-don/search")
    public ApiResponse<PageResponse<HoaDonResponse>> searchInvoices(@RequestParam(required = false) String period,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return ApiResponse.<PageResponse<HoaDonResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        hoaDonService.searchTenantInvoices(tenant.getKhachThueId(), period, status, page, size)
                                .map(hoaDonMapper::toHoaDonResponse)
                ))
                .build();
    }

    @GetMapping("/thanh-toan")
    public ApiResponse<PageResponse<ThanhToanResponse>> getPayments(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return ApiResponse.<PageResponse<ThanhToanResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        thanhToanService.getTenantPayments(tenant.getKhachThueId(), page, size, sortBy, direction)
                                .map(thanhToanMapper::toThanhToanResponse)
                ))
                .build();
    }

    @GetMapping("/thanh-toan/search")
    public ApiResponse<PageResponse<ThanhToanResponse>> searchPayments(@RequestParam(required = false) String period,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return ApiResponse.<PageResponse<ThanhToanResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        thanhToanService.searchTenantPayments(tenant.getKhachThueId(), period, status, page, size)
                                .map(thanhToanMapper::toThanhToanResponse)
                ))
                .build();
    }

    @GetMapping("/chi-so")
    public ApiResponse<PageResponse<ChiSoResponse>> getMeters(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String sortBy,
                                    @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        List<Long> roomIds = hopDongService.getTenantRoomIds(tenant.getKhachThueId());
        return ApiResponse.<PageResponse<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        chiSoService.getTenantMeters(roomIds, page, size, sortBy, direction).map(chiSoMapper::toChiSoResponse)
                ))
                .build();
    }

    @GetMapping("/chi-so/search")
    public ApiResponse<PageResponse<ChiSoResponse>> searchMeters(@RequestParam(required = false) String type,
                                       @RequestParam(required = false) String period,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        List<Long> roomIds = hopDongService.getTenantRoomIds(tenant.getKhachThueId());
        return ApiResponse.<PageResponse<ChiSoResponse>>builder()
                .code(200)
                .message("success")
                .result(PageResponse.from(
                        chiSoService.searchTenantMeters(roomIds, type, period, page, size).map(chiSoMapper::toChiSoResponse)
                ))
                .build();
    }
}

