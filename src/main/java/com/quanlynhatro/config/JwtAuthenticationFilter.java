package com.quanlynhatro.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private static final String AUTH_COOKIE = "QLPT_AUTH";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String headerToken = resolveHeaderToken(request);
        final String cookieToken = resolveCookieToken(request);

        if ((headerToken == null || headerToken.isBlank()) && (cookieToken == null || cookieToken.isBlank())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String[] candidates = { headerToken, cookieToken };
            for (String jwt : candidates) {
                if (jwt == null || jwt.isBlank()) {
                    continue;
                }

                try {
                    String username = jwtService.extractUsername(jwt);
                    if (username == null || username.isBlank()) {
                        continue;
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (!jwtService.isTokenValid(jwt, userDetails)) {
                        continue;
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    break;
                } catch (UsernameNotFoundException ex) {
                    SecurityContextHolder.clearContext();
                } catch (Exception ex) {
                    // Ignore invalid token candidate and continue to next source (header -> cookie).
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveHeaderToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String resolveCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie != null && AUTH_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
