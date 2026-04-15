package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreateHopDongRequest;
import com.quanlynhatro.dto.request.UpdateHopDongRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.HopDongResponse;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.mapper.HopDongMapper;
import com.quanlynhatro.mapper.PhongTroMapper;
import com.quanlynhatro.service.HopDongService;
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

import java.util.List;

@RestController
@RequestMapping("/api/hop-dong")
@RequiredArgsConstructor
public class HopDongController extends ApiControllerSupport {

    private final HopDongService hopDongService;
    private final HopDongMapper hopDongMapper;
    private final PhongTroMapper phongTroMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                hopDongService::getAll,
                () -> hopDongService.getPage(page, size, sortBy, direction),
                hopDongMapper::toResponse
        );
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false) String room,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return success(hopDongService.search(room, status, page, size).map(hopDongMapper::toResponse));
    }

    @GetMapping("/phong-trong")
    public ApiResponse<List<?>> getAvailableRooms() {
        List<PhongTro> rooms = hopDongService.getAvailableRooms();
        return success(rooms.stream().map(phongTroMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<HopDongResponse> getById(@PathVariable Long id) {
        return success(hopDongMapper.toResponse(hopDongService.getById(id)));
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<?> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> hopDongService.getByPhongTroId(phongTroId),
                () -> hopDongService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction),
                hopDongMapper::toResponse
        );
    }

    @GetMapping("/khach/{khachThueId}")
    public ApiResponse<?> getByKhachThue(@PathVariable Long khachThueId,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> hopDongService.getByKhachThueId(khachThueId),
                () -> hopDongService.getPageByKhachThueId(khachThueId, page, size, sortBy, direction),
                hopDongMapper::toResponse
        );
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ApiResponse<?> getByTrangThai(@PathVariable String trangThai,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> hopDongService.getByTrangThai(trangThai),
                () -> hopDongService.getPageByTrangThai(trangThai, page, size, sortBy, direction),
                hopDongMapper::toResponse
        );
    }

    @PostMapping
    public ApiResponse<HopDongResponse> create(@Valid @RequestBody CreateHopDongRequest request) {
        return success(hopDongMapper.toResponse(hopDongService.createFromRequest(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<HopDongResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateHopDongRequest request) {
        return success(hopDongMapper.toResponse(hopDongService.update(id, request)));
    }

    @PutMapping("/{id}/ket-thuc")
    public ApiResponse<HopDongResponse> ketThucHopDong(@PathVariable Long id) {
        return success(hopDongMapper.toResponse(hopDongService.ketThucHopDong(id)));
    }

    @PutMapping("/{id}/huy")
    public ApiResponse<HopDongResponse> huyHopDong(@PathVariable Long id) {
        return success(hopDongMapper.toResponse(hopDongService.huyHopDong(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hopDongService.delete(id);
        return successMessage("Xoa hop dong thanh cong");
    }
}
