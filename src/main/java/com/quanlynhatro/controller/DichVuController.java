package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreateDichVuRequest;
import com.quanlynhatro.dto.request.UpdateDichVuRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.DichVuResponse;
import com.quanlynhatro.entity.DichVu;
import com.quanlynhatro.mapper.DichVuMapper;
import com.quanlynhatro.service.DichVuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dich-vu")
@RequiredArgsConstructor
public class DichVuController extends ApiControllerSupport {

    private final DichVuService dichVuService;
    private final DichVuMapper dichVuMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                dichVuService::getAll,
                () -> dichVuService.getPage(page, size, sortBy, direction),
                dichVuMapper::toResponse
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<DichVuResponse> getById(@PathVariable Long id) {
        return success(dichVuMapper.toResponse(dichVuService.getById(id)));
    }

    @PostMapping
    public ApiResponse<DichVuResponse> create(@Valid @RequestBody CreateDichVuRequest request) {
        DichVu dichVu = new DichVu();
        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setGiaDichVu(request.getGiaDichVu());
        return success(dichVuMapper.toResponse(dichVuService.create(dichVu)));
    }

    @PutMapping("/{id}")
    public ApiResponse<DichVuResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateDichVuRequest request) {
        DichVu dichVu = new DichVu();
        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setGiaDichVu(request.getGiaDichVu());
        return success(dichVuMapper.toResponse(dichVuService.update(id, dichVu)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dichVuService.delete(id);
        return successMessage("Xoa dich vu thanh cong");
    }
}
