package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.TenantProfileResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController extends ApiControllerSupport {

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
        return success(TenantProfileResponse.from(currentUserService.getCurrentTenant()));
    }

    @GetMapping("/hop-dong")
    public ApiResponse<?> getContracts(@RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String sortBy,
                                       @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return success(hopDongService.getTenantContracts(tenant.getKhachThueId(), page, size, sortBy, direction).map(hopDongMapper::toResponse));
    }

    @GetMapping("/hoa-don")
    public ApiResponse<?> getInvoices(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return success(hoaDonService.getTenantInvoices(tenant.getKhachThueId(), page, size, sortBy, direction).map(hoaDonMapper::toResponse));
    }

    @GetMapping("/hoa-don/search")
    public ApiResponse<?> searchInvoices(@RequestParam(required = false) String period,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return success(hoaDonService.searchTenantInvoices(tenant.getKhachThueId(), period, status, page, size).map(hoaDonMapper::toResponse));
    }

    @GetMapping("/thanh-toan")
    public ApiResponse<?> getPayments(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return success(thanhToanService.getTenantPayments(tenant.getKhachThueId(), page, size, sortBy, direction).map(thanhToanMapper::toResponse));
    }

    @GetMapping("/thanh-toan/search")
    public ApiResponse<?> searchPayments(@RequestParam(required = false) String period,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        return success(thanhToanService.searchTenantPayments(tenant.getKhachThueId(), period, status, page, size).map(thanhToanMapper::toResponse));
    }

    @GetMapping("/chi-so")
    public ApiResponse<?> getMeters(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String sortBy,
                                    @RequestParam(required = false) String direction) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        List<Long> roomIds = hopDongService.getTenantRoomIds(tenant.getKhachThueId());
        return success(chiSoService.getTenantMeters(roomIds, page, size, sortBy, direction).map(chiSoMapper::toResponse));
    }

    @GetMapping("/chi-so/search")
    public ApiResponse<?> searchMeters(@RequestParam(required = false) String type,
                                       @RequestParam(required = false) String period,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        KhachThue tenant = currentUserService.getCurrentTenant();
        List<Long> roomIds = hopDongService.getTenantRoomIds(tenant.getKhachThueId());
        return success(chiSoService.searchTenantMeters(roomIds, type, period, page, size).map(chiSoMapper::toResponse));
    }
}
