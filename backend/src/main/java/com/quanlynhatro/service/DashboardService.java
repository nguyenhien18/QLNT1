package com.quanlynhatro.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

import com.quanlynhatro.dto.response.DashboardSummaryResponse;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.entity.ThanhToan;
import com.quanlynhatro.repository.HoaDonRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.repository.ThanhToanRepository;
import com.quanlynhatro.util.RoomTypeUtils;

@Service
public class DashboardService {
    private final PhongTroRepository phongTroRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HopDongRepository hopDongRepository;
    private final ThanhToanRepository thanhToanRepository;

    public DashboardService(PhongTroRepository phongTroRepository,
                            HoaDonRepository hoaDonRepository,
                            HopDongRepository hopDongRepository,
                            ThanhToanRepository thanhToanRepository) {
        this.phongTroRepository = phongTroRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.hopDongRepository = hopDongRepository;
        this.thanhToanRepository = thanhToanRepository;
    }

    public DashboardSummaryResponse getSummary() {
        List<PhongTro> rooms = phongTroRepository.findAll();
        long totalRooms = rooms.size();
        long rentedRooms = rooms.stream()
                .filter(room -> room.getTrangThai() == PhongTro.TrangThai.DA_CHO_THUE)
                .count();
        long emptyRooms = Math.max(0L, totalRooms - rentedRooms);
        long totalVipRooms = rooms.stream()
                .filter(room -> RoomTypeUtils.isVip(room.getLoaiPhong()))
                .count();
        long rentedVipRooms = rooms.stream()
                .filter(room -> RoomTypeUtils.isVip(room.getLoaiPhong()))
                .filter(room -> room.getTrangThai() == PhongTro.TrangThai.DA_CHO_THUE)
                .count();
        long unpaidInvoices = hoaDonRepository.countByTrangThai(HoaDon.TrangThai.CHUA_THANH_TOAN);
        long expiringContracts = countExpiringContracts(30);

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        BigDecimal yearlyRevenue = BigDecimal.ZERO;
        List<ThanhToan> successfulPayments = thanhToanRepository.findByTrangThai(ThanhToan.TrangThai.THANH_CONG);
        for (ThanhToan payment : successfulPayments) {
            LocalDate paidDate = payment.getNgayThanhToan();
            if (paidDate == null || paidDate.getYear() != currentYear) {
                continue;
            }
            BigDecimal amount = payment.getSoTien() == null ? BigDecimal.ZERO : payment.getSoTien();
            yearlyRevenue = yearlyRevenue.add(amount);
            if (paidDate.getMonthValue() == currentMonth) {
                monthlyRevenue = monthlyRevenue.add(amount);
            }
        }

        return new DashboardSummaryResponse(
                totalRooms,
                rentedRooms,
                emptyRooms,
                totalVipRooms,
                rentedVipRooms,
                unpaidInvoices,
                expiringContracts,
                monthlyRevenue,
                yearlyRevenue
        );
    }

    private long countExpiringContracts(int maxDays) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(Math.max(0, maxDays));
        return hopDongRepository.findByTrangThai(HopDong.TrangThai.CON_HIEU_LUC).stream()
                .filter(contract -> contract.getNgayKetThuc() != null)
                .filter(contract -> !contract.getNgayKetThuc().isBefore(today))
                .filter(contract -> !contract.getNgayKetThuc().isAfter(end))
                .count();
    }
}
