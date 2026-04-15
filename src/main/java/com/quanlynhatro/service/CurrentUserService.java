package com.quanlynhatro.service;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.KhachThueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final KhachThueRepository khachThueRepository;

    public KhachThue getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Ban chua dang nhap");
        }

        return khachThueRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "Khong xac dinh duoc nguoi thue hien tai"));
    }
}


