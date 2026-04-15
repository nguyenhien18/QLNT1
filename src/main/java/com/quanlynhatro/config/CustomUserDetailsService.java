package com.quanlynhatro.config;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.KhachThue;
import com.quanlynhatro.repository.ChuTroRepository;
import com.quanlynhatro.repository.KhachThueRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ChuTroRepository chuTroRepository;
    private final KhachThueRepository khachThueRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ChuTro chuTro = chuTroRepository.findByEmail(username).orElse(null);
        if (chuTro != null) {
            return new User(
                    chuTro.getEmail(),
                    chuTro.getMatKhau(),
                    List.of(new SimpleGrantedAuthority(AppRoles.LANDLORD_AUTHORITY))
            );
        }

        KhachThue khachThue = khachThueRepository.findByTenDangNhap(username).orElse(null);
        if (khachThue != null) {
            if (khachThue.getTrangThai() != KhachThue.TrangThai.HOAT_DONG) {
                throw new UsernameNotFoundException("Tai khoan nguoi thue da bi khoa: " + username);
            }
            return new User(
                    khachThue.getTenDangNhap(),
                    khachThue.getMatKhau(),
                    List.of(new SimpleGrantedAuthority(AppRoles.TENANT_AUTHORITY))
            );
        }

        throw new UsernameNotFoundException("Khong tim thay tai khoan: " + username);
    }
}


