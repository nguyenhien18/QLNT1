package com.quanlynhatro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChuTroServiceTest {

    @Mock
    private ChuTroRepository chuTroRepository;
    @Mock
    private PhongTroRepository phongTroRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ChuTroService chuTroService;

    @BeforeEach
    void setUp() {
        chuTroService = new ChuTroService(chuTroRepository, phongTroRepository, passwordEncoder);
    }

    @Test
    void createShouldRejectDuplicateEmail() {
        ChuTro payload = new ChuTro();
        payload.setHoTen("Owner");
        payload.setEmail("owner@example.com");
        payload.setMatKhau("secret123");

        when(chuTroRepository.existsByEmail("owner@example.com")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> chuTroService.create(payload));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(chuTroRepository, never()).save(payload);
    }

    @Test
    void createShouldRejectDuplicatePhone() {
        ChuTro payload = new ChuTro();
        payload.setHoTen("Owner");
        payload.setSdt("0900000000");
        payload.setMatKhau("secret123");

        when(chuTroRepository.existsBySdt("0900000000")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> chuTroService.create(payload));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(chuTroRepository, never()).save(payload);
    }

    @Test
    void deleteShouldRejectWhenLandlordHasRooms() {
        ChuTro owner = new ChuTro();
        owner.setChuTroId(1L);
        owner.setHoTen("Owner");
        owner.setMatKhau("x");
        when(chuTroRepository.findById(1L)).thenReturn(java.util.Optional.of(owner));
        when(phongTroRepository.existsByChuTro_ChuTroId(1L)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> chuTroService.delete(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(chuTroRepository, never()).deleteById(1L);
    }
}

