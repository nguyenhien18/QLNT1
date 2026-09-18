package com.quanlynhatro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.KhachThueRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private KhachThueRepository khachThueRepository;

    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserService(khachThueRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentTenantShouldThrowUnauthorizedWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        AppException ex = assertThrows(AppException.class, () -> currentUserService.getCurrentTenant());

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void getCurrentTenantShouldThrowForbiddenWhenTenantNotFound() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tenant01", "n/a")
        );
        when(khachThueRepository.findByTenDangNhap("tenant01")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> currentUserService.getCurrentTenant());

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void getCurrentTenantShouldReturnTenantFromAuthenticationName() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tenant01", "n/a")
        );
        KhachThue tenant = new KhachThue();
        tenant.setKhachThueId(7L);
        tenant.setTenDangNhap("tenant01");
        when(khachThueRepository.findByTenDangNhap("tenant01")).thenReturn(Optional.of(tenant));

        KhachThue result = currentUserService.getCurrentTenant();

        assertEquals(7L, result.getKhachThueId());
    }
}

