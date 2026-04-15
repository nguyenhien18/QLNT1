package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.PhongDichVuRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PhongDichVuResponse;
import com.quanlynhatro.mapper.PhongDichVuMapper;
import com.quanlynhatro.service.PhongDichVuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/phong-dich-vu")
@RequiredArgsConstructor
public class PhongDichVuController extends ApiControllerSupport {

    private final PhongDichVuService phongDichVuService;
    private final PhongDichVuMapper phongDichVuMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                phongDichVuService::getAll,
                () -> phongDichVuService.getPage(page, size, sortBy, direction),
                phongDichVuMapper::toResponse
        );
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<?> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> phongDichVuService.getByPhongTroId(phongTroId),
                () -> phongDichVuService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction),
                phongDichVuMapper::toResponse
        );
    }

    @GetMapping("/dich-vu/{dichVuId}")
    public ApiResponse<?> getByDichVu(@PathVariable Long dichVuId,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> phongDichVuService.getByDichVuId(dichVuId),
                () -> phongDichVuService.getPageByDichVuId(dichVuId, page, size, sortBy, direction),
                phongDichVuMapper::toResponse
        );
    }

    @PostMapping
    public ApiResponse<PhongDichVuResponse> create(@Valid @RequestBody PhongDichVuRequest request) {
        return success(phongDichVuMapper.toResponse(phongDichVuService.create(request)));
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam Long phongTroId, @RequestParam Long dichVuId) {
        phongDichVuService.delete(phongTroId, dichVuId);
        return successMessage("Xoa dich vu khoi phong thanh cong");
    }
}
