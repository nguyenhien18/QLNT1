package com.quanlynhatro.controller;

import com.quanlynhatro.util.PageableUtils;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PageResponse;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.data.domain.Page;

public abstract class ApiControllerSupport {

    protected <T> ApiResponse<?> paged(
            Integer page,
            Integer size,
            String sortBy,
            String direction,
            Supplier<List<T>> listFn,
            Supplier<Page<T>> pageFn
    ) {
        Object data = PageableUtils.hasPagination(page, size, sortBy, direction)
                ? PageResponse.from(pageFn.get())
                : listFn.get();
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(data)
                .build();
    }

    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .result(data)
                .build();
    }

    protected <T, R> ApiResponse<?> pagedMapped(
            Integer page,
            Integer size,
            String sortBy,
            String direction,
            Supplier<List<T>> listFn,
            Supplier<Page<T>> pageFn,
            Function<T, R> mapper
    ) {
        Object data = PageableUtils.hasPagination(page, size, sortBy, direction)
                ? PageResponse.from(pageFn.get().map(mapper))
                : listFn.get().stream().map(mapper).toList();
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(data)
                .build();
    }

    protected ApiResponse<Void> successMessage(String message) {
        return ApiResponse.<Void>builder()
                .code(200)
                .message(message)
                .build();
    }
}
