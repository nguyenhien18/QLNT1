package com.quanlynhatro.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageableUtils {
    private PageableUtils() {
    }

    public static boolean hasPagination(Integer page, Integer size, String sortBy, String direction) {
        return page != null || size != null || sortBy != null || direction != null;
    }

    public static Pageable build(Integer page, Integer size, String sortBy, String direction, String defaultSortBy) {
        int resolvedPage = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null || size <= 0 ? 10 : Math.min(size, 100);
        String resolvedSortBy = (sortBy == null || sortBy.isBlank()) ? defaultSortBy : sortBy;
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(sortDirection, resolvedSortBy));
    }
}
