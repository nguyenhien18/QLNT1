package com.quanlynhatro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class KhachThueServiceTest {

    @Mock
    private KhachThueRepository khachThueRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private HopDongRepository hopDongRepository;
    @Mock
    private ThanhVienPhongRepository thanhVienPhongRepository;

    private KhachThueService khachThueService;

    @BeforeEach
    void setUp() {
        khachThueService = new KhachThueService(
                khachThueRepository,
                passwordEncoder,
                hopDongRepository,
                thanhVienPhongRepository
        );
    }

    @Test
    void createShouldRejectDuplicateUsername() {
        KhachThue payload = new KhachThue();
        payload.setHoTen("Tenant A");
        payload.setTenDangNhap("tenant1");
        payload.setMatKhau("secret123");

        when(khachThueRepository.existsByTenDangNhap("tenant1")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> khachThueService.create(payload));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(khachThueRepository, never()).save(payload);
    }

    @Test
    void createShouldRejectDuplicateCccd() {
        KhachThue payload = new KhachThue();
        payload.setHoTen("Tenant A");
        payload.setCccd("012345678901");
        payload.setTenDangNhap("tenant2");
        payload.setMatKhau("secret123");

        when(khachThueRepository.existsByCccd("012345678901")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> khachThueService.create(payload));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(khachThueRepository, never()).save(payload);
    }

    @Test
    void updateShouldRejectDuplicateEmail() {
        KhachThue current = new KhachThue();
        current.setKhachThueId(10L);
        current.setHoTen("Tenant A");
        current.setTenDangNhap("tenant10");
        current.setMatKhau("encoded");
        current.setTrangThai(KhachThue.TrangThai.HOAT_DONG);

        var request = new com.quanlynhatro.dto.request.KhachThueUpdateRequest();
        request.setEmail("dupe@example.com");

        when(khachThueRepository.findById(10L)).thenReturn(java.util.Optional.of(current));
        when(hopDongRepository.existsByKhachThue_KhachThueIdAndTrangThai(10L, com.quanlynhatro.entity.HopDong.TrangThai.CON_HIEU_LUC)).thenReturn(false);
        when(thanhVienPhongRepository.existsByKhachThue_KhachThueIdAndHopDong_TrangThai(10L, com.quanlynhatro.entity.HopDong.TrangThai.CON_HIEU_LUC)).thenReturn(false);
        when(khachThueRepository.existsByEmailAndKhachThueIdNot("dupe@example.com", 10L)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> khachThueService.update(10L, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(khachThueRepository, never()).save(current);
    }
}

