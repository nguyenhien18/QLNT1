package com.quanlynhatro.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.DashboardSummaryResponse;
import com.quanlynhatro.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {
    private final DashboardService dashboardService;

    @GetMapping("/tong-quan")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .code(200)
                .message("success")
                .result(dashboardService.getSummary())
                .build();
    }
}

