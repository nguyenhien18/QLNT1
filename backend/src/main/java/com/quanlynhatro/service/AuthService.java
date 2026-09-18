package com.quanlynhatro.service;

import java.util.function.Consumer;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.quanlynhatro.config.AppRoles;
import com.quanlynhatro.config.CustomUserDetailsService;
import com.quanlynhatro.config.JwtService;
import com.quanlynhatro.dto.request.LoginRequest;
import com.quanlynhatro.dto.response.AuthProfileResponse;
import com.quanlynhatro.dto.response.LoginResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.KhachThueRepository;

@Service
public class AuthService {
    private final ChuTroRepository chuTroRepository;
    private final KhachThueRepository khachThueRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthService(
            ChuTroRepository chuTroRepository,
            KhachThueRepository khachThueRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.chuTroRepository = chuTroRepository;
        this.khachThueRepository = khachThueRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    public LoginResponse loginAdmin(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String rawPassword = request.getPassword() != null ? request.getPassword() : "";

        ChuTro chuTro = chuTroRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau"));

        verifyAndUpgradePasswordIfNeeded(chuTro.getMatKhau(), rawPassword, encoded -> {
            chuTro.setMatKhau(encoded);
            chuTroRepository.save(chuTro);
        });

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(chuTro.getEmail());
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
                .or(() -> khachThueRepository.findByEmail(username))
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Sai tai khoan hoac mat khau"));

        if (khachThue.getTrangThai() != KhachThue.TrangThai.HOAT_DONG) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tai khoan nguoi thue da bi khoa");
        }

        verifyAndUpgradePasswordIfNeeded(khachThue.getMatKhau(), rawPassword, encoded -> {
            khachThue.setMatKhau(encoded);
            khachThueRepository.save(khachThue);
        });

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(khachThue.getTenDangNhap());
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

    public AuthProfileResponse getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getAuthorities() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Ban chua dang nhap");
        }

        String username = authentication.getName();
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (authorities.contains(AppRoles.LANDLORD_AUTHORITY)) {
            ChuTro chuTro = chuTroRepository.findByEmail(username)
                    .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Khong xac dinh duoc chu tro hien tai"));
            return new AuthProfileResponse(
                    chuTro.getChuTroId(),
                    "ADMIN",
                    chuTro.getEmail(),
                    chuTro.getHoTen()
            );
        }

        if (authorities.contains(AppRoles.TENANT_AUTHORITY)) {
            KhachThue khachThue = khachThueRepository.findByTenDangNhap(username)
                    .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Khong xac dinh duoc nguoi thue hien tai"));
            if (khachThue.getTrangThai() != KhachThue.TrangThai.HOAT_DONG) {
                throw new AppException(HttpStatus.FORBIDDEN, "Tai khoan nguoi thue da bi khoa");
            }
            return new AuthProfileResponse(
                    khachThue.getKhachThueId(),
                    "USER",
                    khachThue.getTenDangNhap(),
                    khachThue.getHoTen()
            );
        }

        throw new AppException(HttpStatus.FORBIDDEN, "Khong co quyen truy cap");
    }
}
