package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.ChuTroUpdateRequest;
import com.quanlynhatro.dto.request.CreateChuTroRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.ChuTroResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.mapper.ChuTroMapper;
import com.quanlynhatro.service.ChuTroService;
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
@RequestMapping("/api/chu-tro")
@RequiredArgsConstructor
public class ChuTroController extends ApiControllerSupport {

    private final ChuTroService chuTroService;
    private final ChuTroMapper chuTroMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                chuTroService::getAll,
                () -> chuTroService.getPage(page, size, sortBy, direction),
                chuTroMapper::toResponse
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ChuTroResponse> getById(@PathVariable Long id) {
        return success(chuTroMapper.toResponse(chuTroService.getById(id)));
    }

    @GetMapping("/email")
    public ApiResponse<ChuTroResponse> getByEmail(@RequestParam String email) {
        return success(chuTroMapper.toResponse(chuTroService.getByEmail(email)));
    }

    @PostMapping
    public ApiResponse<ChuTroResponse> create(@Valid @RequestBody CreateChuTroRequest request) {
        ChuTro chuTro = new ChuTro();
        chuTro.setHoTen(request.getHoTen());
        chuTro.setEmail(request.getEmail());
        chuTro.setSdt(request.getSdt());
        chuTro.setMatKhau(request.getMatKhau());
        return success(chuTroMapper.toResponse(chuTroService.create(chuTro)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChuTroResponse> update(@PathVariable Long id, @Valid @RequestBody ChuTroUpdateRequest request) {
        return success(chuTroMapper.toResponse(chuTroService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        chuTroService.delete(id);
        return successMessage("Xoa chu tro thanh cong");
    }
}
