package com.quanlynhatro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quanlynhatro.config.CustomUserDetailsService;
import com.quanlynhatro.config.JwtService;
import com.quanlynhatro.dto.request.LoginRequest;
import com.quanlynhatro.dto.response.LoginResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ChuTroRepository chuTroRepository;
    @Mock
    private KhachThueRepository khachThueRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                chuTroRepository,
                khachThueRepository,
                passwordEncoder,
                jwtService,
                customUserDetailsService
        );
    }

    @Test
    void loginAdminShouldReturnTokenWhenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin@qlpt.vn");
        request.setPassword("secret123");

        ChuTro owner = new ChuTro();
        owner.setChuTroId(1L);
        owner.setEmail("admin@qlpt.vn");
        owner.setHoTen("Chu tro A");
        owner.setMatKhau("$2a$encoded");

        User userDetails = new User("admin@qlpt.vn", "x", java.util.List.of());

        when(chuTroRepository.findByEmail("admin@qlpt.vn")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("secret123", "$2a$encoded")).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername("admin@qlpt.vn")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.loginAdmin(request);

        assertEquals("ADMIN", response.getRole());
        assertEquals("jwt-token", response.getToken());
        verify(chuTroRepository, never()).save(owner);
    }

    @Test
    void loginUserShouldUpgradePlainPasswordWhenMatched() {
        LoginRequest request = new LoginRequest();
        request.setUsername("tenant01");
        request.setPassword("old-pass");

        KhachThue tenant = new KhachThue();
        tenant.setKhachThueId(11L);
        tenant.setTenDangNhap("tenant01");
        tenant.setHoTen("Nguoi thue A");
        tenant.setMatKhau("old-pass");
        tenant.setTrangThai(KhachThue.TrangThai.HOAT_DONG);

        User userDetails = new User("tenant01", "x", java.util.List.of());

        when(khachThueRepository.findByTenDangNhap("tenant01")).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode("old-pass")).thenReturn("$2a$newhash");
        when(customUserDetailsService.loadUserByUsername("tenant01")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-user");

        LoginResponse response = authService.loginUser(request);

        assertEquals("USER", response.getRole());
        assertEquals("jwt-user", response.getToken());
        assertEquals("$2a$newhash", tenant.getMatKhau());
        verify(khachThueRepository).save(tenant);
    }

    @Test
    void loginUserShouldRejectWhenTenantBlocked() {
        LoginRequest request = new LoginRequest();
        request.setUsername("tenant-locked");
        request.setPassword("secret");

        KhachThue tenant = new KhachThue();
        tenant.setTenDangNhap("tenant-locked");
        tenant.setMatKhau("$2a$hash");
        tenant.setTrangThai(KhachThue.TrangThai.KHOA);

        when(khachThueRepository.findByTenDangNhap("tenant-locked")).thenReturn(Optional.of(tenant));

        AppException ex = assertThrows(AppException.class, () -> authService.loginUser(request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void loginAdminShouldRejectWhenBcryptMismatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin@qlpt.vn");
        request.setPassword("wrong");

        ChuTro owner = new ChuTro();
        owner.setEmail("admin@qlpt.vn");
        owner.setMatKhau("$2a$encoded");

        when(chuTroRepository.findByEmail("admin@qlpt.vn")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("wrong", "$2a$encoded")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.loginAdmin(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}

