package com.quanlynhatro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.repository.ChiSoRepository;
import com.quanlynhatro.repository.HoaDonRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import com.quanlynhatro.repository.PhongDichVuRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.repository.ThanhToanRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class TenantScopeServiceTest {

    @Mock
    private HopDongRepository hopDongRepository;
    @Mock
    private PhongTroRepository phongTroRepository;
    @Mock
    private KhachThueRepository khachThueRepository;
    @Mock
    private ThanhVienPhongRepository thanhVienPhongRepository;
    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private ThanhToanRepository thanhToanRepository;
    @Mock
    private ChiSoRepository chiSoRepository;
    @Mock
    private PhongDichVuRepository phongDichVuRepository;

    private HopDongService hopDongService;
    private HoaDonService hoaDonService;
    private ChiSoService chiSoService;

    @BeforeEach
    void setUp() {
        hopDongService = new HopDongService(
                hopDongRepository,
                phongTroRepository,
                khachThueRepository,
                thanhVienPhongRepository,
                hoaDonRepository,
                thanhToanRepository
        );
        hoaDonService = new HoaDonService(
                hoaDonRepository,
                hopDongRepository,
                phongTroRepository,
                chiSoRepository,
                phongDichVuRepository,
                thanhVienPhongRepository,
                thanhToanRepository
        );
        chiSoService = new ChiSoService(chiSoRepository, hopDongRepository, hoaDonRepository);
    }

    @Test
    void getTenantRoomIdsShouldReturnDistinctNonNullRoomIdsFromAccessibleContracts() {
        HopDong c1 = new HopDong();
        c1.setPhongTro(room(10L));
        HopDong c2 = new HopDong();
        c2.setPhongTro(room(10L));
        HopDong c3 = new HopDong();
        c3.setPhongTro(room(11L));
        HopDong c4 = new HopDong();
        c4.setPhongTro(null);
        when(hopDongRepository.findAccessibleByKhachThueIdAndTrangThai(99L, HopDong.TrangThai.CON_HIEU_LUC))
                .thenReturn(List.of(c1, c2, c3, c4));

        List<Long> roomIds = hopDongService.getTenantRoomIds(99L);

        assertEquals(List.of(10L, 11L), roomIds);
    }

    @Test
    void searchTenantInvoicesShouldQueryRepositoryWithTenantId() {
        Page<HoaDon> emptyPage = new PageImpl<>(List.of());
        when(hoaDonRepository.searchAccessibleByKhachThueId(eq(88L), eq(HoaDon.TrangThai.DA_THANH_TOAN), eq("2026-04"), any()))
                .thenReturn(emptyPage);

        Page<HoaDon> result = hoaDonService.searchTenantInvoices(88L, "2026-04", "PAID", 0, 10);

        assertTrue(result.isEmpty());
        verify(hoaDonRepository).searchAccessibleByKhachThueId(eq(88L), eq(HoaDon.TrangThai.DA_THANH_TOAN), eq("2026-04"), any());
    }

    @Test
    void searchTenantMetersShouldReturnEmptyPageWhenRoomIdsEmpty() {
        var result = chiSoService.searchTenantMeters(List.of(), "DIEN", "2026-04", 0, 10);

        assertTrue(result.isEmpty());
        verify(chiSoRepository, never()).searchByRoomIds(any(), any(), any(), any());
    }

    private PhongTro room(Long roomId) {
        PhongTro room = new PhongTro();
        room.setPhongTroId(roomId);
        return room;
    }
}

