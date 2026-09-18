package com.quanlynhatro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.HopDongRepository;
import com.quanlynhatro.repository.PhongDichVuRepository;
import com.quanlynhatro.repository.PhongTroRepository;
import com.quanlynhatro.repository.ThanhVienPhongRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PhongTroServiceTest {
    @Mock
    private PhongTroRepository phongTroRepository;
    @Mock
    private ChuTroRepository chuTroRepository;
    @Mock
    private HopDongRepository hopDongRepository;
    @Mock
    private ThanhVienPhongRepository thanhVienPhongRepository;
    @Mock
    private PhongDichVuRepository phongDichVuRepository;

    private PhongTroService phongTroService;

    @BeforeEach
    void setUp() {
        phongTroService = new PhongTroService(
                phongTroRepository,
                chuTroRepository,
                hopDongRepository,
                thanhVienPhongRepository,
                phongDichVuRepository
        );
    }

    @Test
    void createShouldNormalizeSupportedRoomType() {
        PhongTro payload = room("thuong");
        ChuTro owner = new ChuTro();
        owner.setChuTroId(1L);

        when(chuTroRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(phongTroRepository.save(any(PhongTro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PhongTro saved = phongTroService.create(payload);

        assertEquals("THUONG", saved.getLoaiPhong());
    }

    @Test
    void createShouldRejectLegacyRoomType() {
        PhongTro payload = room("CO_GAC");
        ChuTro owner = new ChuTro();
        owner.setChuTroId(1L);

        when(chuTroRepository.findById(1L)).thenReturn(Optional.of(owner));

        AppException ex = assertThrows(AppException.class, () -> phongTroService.create(payload));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(phongTroRepository, never()).save(any(PhongTro.class));
    }

    private PhongTro room(String roomType) {
        ChuTro owner = new ChuTro();
        owner.setChuTroId(1L);

        PhongTro room = new PhongTro();
        room.setChuTro(owner);
        room.setTenPhong("P101");
        room.setLoaiPhong(roomType);
        room.setGiaThue(BigDecimal.valueOf(3000000));
        room.setSucChua(2);
        room.setTrangThai(PhongTro.TrangThai.TRONG);
        return room;
    }
}
