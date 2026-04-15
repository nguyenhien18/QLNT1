package com.quanlynhatro.service;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.config.CustomUserDetailsService;
import com.quanlynhatro.config.JwtService;
import com.quanlynhatro.dto.request.LoginRequest;
import com.quanlynhatro.dto.response.LoginResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import java.util.function.Consumer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ChuTroRepository chuTroRepository;
    private final KhachThueRepository khachThueRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService custaomUserDetailsService;

    public LoginResponse loginAdmin(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String rawPassword = request.getPassword() != null ? request.getPassword() : "";

        ChuTro chuTro = chuTroRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau"));

        verifyAndUpgradePasswordIfNeeded(chuTro.getMatKhau(), rawPassword, encoded -> {
            chuTro.setMatKhau(encoded);
            chuTroRepository.save(chuTro);
        });

        UserDetails userDetails = custaomUserDetailsService.loadUserByUsername(chuTro.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(
                chuTro.getChuTroId(),
                "ADMIN",
                chuTro.getEmail(),
                chuTro.getHoTen(),
                "Dang nhap admin thanh cong",
                token
        );
    }

    public LoginResponse loginUser(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String rawPassword = request.getPassword() != null ? request.getPassword() : "";

        KhachThue khachThue = khachThueRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau"));

        if (khachThue.getTrangThai() != KhachThue.TrangThai.HOAT_DONG) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tai khoan nguoi thue da bi khoa");
        }

        verifyAndUpgradePasswordIfNeeded(khachThue.getMatKhau(), rawPassword, encoded -> {
            khachThue.setMatKhau(encoded);
            khachThueRepository.save(khachThue);
        });

        UserDetails userDetails = custaomUserDetailsService.loadUserByUsername(khachThue.getTenDangNhap());
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(
                khachThue.getKhachThueId(),
                "USER",
                khachThue.getTenDangNhap(),
                khachThue.getHoTen(),
                "Dang nhap user thanh cong",
                token
        );
    }

    private void verifyAndUpgradePasswordIfNeeded(String storedPassword,
                                                  String rawPassword,
                                                  Consumer<String> upgradeAction) {
        if (storedPassword == null || rawPassword == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau");
        }

        boolean isBcryptHash = storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");

        if (isBcryptHash) {
            if (!passwordEncoder.matches(rawPassword, storedPassword)) {
                throw new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau");
            }
        } else {
            if (!storedPassword.equals(rawPassword)) {
                throw new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau");
            }
            upgradeAction.accept(passwordEncoder.encode(rawPassword));
        }
    }
}


