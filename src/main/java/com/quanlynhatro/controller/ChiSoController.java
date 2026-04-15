package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreateChiSoRequest;
import com.quanlynhatro.dto.request.UpdateChiSoRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.ChiSoResponse;
import com.quanlynhatro.mapper.ChiSoMapper;
import com.quanlynhatro.mapper.ChiSoRequestMapper;
import com.quanlynhatro.service.ChiSoService;
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
@RequestMapping("/api/chi-so")
@RequiredArgsConstructor
public class ChiSoController extends ApiControllerSupport {

    private final ChiSoService chiSoService;
    private final ChiSoMapper chiSoMapper;
    private final ChiSoRequestMapper chiSoRequestMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                chiSoService::getAll,
                () -> chiSoService.getPage(page, size, sortBy, direction),
                chiSoMapper::toResponse
        );
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false) String type,
                                 @RequestParam(required = false) String room,
                                 @RequestParam(required = false) String period,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return success(chiSoService.search(type, room, period, page, size).map(chiSoMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChiSoResponse> getById(@PathVariable Long id) {
        return success(chiSoMapper.toResponse(chiSoService.getById(id)));
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<?> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> chiSoService.getByPhongTroId(phongTroId),
                () -> chiSoService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction),
                chiSoMapper::toResponse
        );
    }

    @GetMapping("/phong/{phongTroId}/ky")
    public ApiResponse<?> getByPhongTroVaKy(@PathVariable Long phongTroId, @RequestParam String ky) {
        return success(chiSoService.getByPhongTroIdAndKy(phongTroId, ky).stream().map(chiSoMapper::toResponse).toList());
    }

    @GetMapping("/phong/{phongTroId}/loai")
    public ApiResponse<?> getByPhongTroVaLoai(@PathVariable Long phongTroId, @RequestParam String loai) {
        return success(chiSoService.getByPhongTroIdAndLoai(phongTroId, loai).stream().map(chiSoMapper::toResponse).toList());
    }

    @PostMapping
    public ApiResponse<ChiSoResponse> create(@Valid @RequestBody CreateChiSoRequest request) {
        return success(chiSoMapper.toResponse(chiSoService.create(chiSoRequestMapper.fromCreateRequest(request))));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChiSoResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateChiSoRequest request) {
        return success(chiSoMapper.toResponse(chiSoService.update(id, chiSoRequestMapper.fromUpdateRequest(request))));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        chiSoService.delete(id);
        return successMessage("Xoa chi so thanh cong");
    }
}
