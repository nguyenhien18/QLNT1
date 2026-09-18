package com.quanlynhatro.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalRooms;
    private long rentedRooms;
    private long emptyRooms;
    private long totalVipRooms;
    private long rentedVipRooms;
    private long unpaidInvoices;
    private long expiringContracts;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
}

