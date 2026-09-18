package com.quanlynhatro.util;

import java.util.Locale;

public final class RoomTypeUtils {
    public static final String THUONG = "THUONG";
    public static final String VIP = "VIP";

    private RoomTypeUtils() {
    }

    public static String normalizeSupported(String roomType) {
        if (roomType == null || roomType.isBlank()) {
            return "";
        }

        String normalized = roomType.trim().toUpperCase(Locale.ROOT);

        if (THUONG.equals(normalized) || VIP.equals(normalized)) {
            return normalized;
        }

        return "";
    }

    public static String canonicalizeForRead(String roomType) {
        if (roomType == null || roomType.isBlank()) {
            return roomType;
        }

        String supported = normalizeSupported(roomType);
        return supported.isBlank() ? roomType.trim().toUpperCase(Locale.ROOT) : supported;
    }

    public static boolean isRegular(String roomType) {
        return THUONG.equals(normalizeSupported(roomType));
    }

    public static boolean isVip(String roomType) {
        return VIP.equals(normalizeSupported(roomType));
    }
}