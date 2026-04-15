package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreatePhongTroRequest;
import com.quanlynhatro.dto.request.UpdatePhongTroRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.PhongTroResponse;
import com.quanlynhatro.entity.ChuTro;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.mapper.PhongTroMapper;
import com.quanlynhatro.service.PhongTroService;
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
@RequestMapping("/api/phong-tro")
@RequiredArgsConstructor
public class PhongTroController extends ApiControllerSupport {

    private final PhongTroService phongTroService;
    private final PhongTroMapper phongTroMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                phongTroService::getAll,
                () -> phongTroService.getPage(page, size, sortBy, direction),
                phongTroMapper::toResponse
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PhongTroResponse> getById(@PathVariable Long id) {
        return success(phongTroMapper.toResponse(phongTroService.getById(id)));
    }

    @GetMapping("/summary")
    public ApiResponse<?> getSummary() {
        return success(phongTroService.getRoomSummaries());
    }

    @GetMapping("/chu-tro/{chuTroId}")
    public ApiResponse<?> getByChuTro(@PathVariable Long chuTroId,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size,
                                      @RequestParam(required = false) String sortBy,
                                      @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> phongTroService.getByChuTroId(chuTroId),
                () -> phongTroService.getPageByChuTroId(chuTroId, page, size, sortBy, direction),
                phongTroMapper::toResponse
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
                () -> phongTroService.getByTrangThai(trangThai),
                () -> phongTroService.getPageByTrangThai(trangThai, page, size, sortBy, direction),
                phongTroMapper::toResponse
        );
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return success(phongTroService.search(name, type, status, keyword, page, size).map(phongTroMapper::toResponse));
    }

    @PostMapping
    public ApiResponse<PhongTroResponse> create(@Valid @RequestBody CreatePhongTroRequest request) {
        return success(phongTroMapper.toResponse(phongTroService.create(toEntity(request))));
    }

    @PutMapping("/{id}")
    public ApiResponse<PhongTroResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePhongTroRequest request) {
        return success(phongTroMapper.toResponse(phongTroService.update(id, toEntity(request))));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        phongTroService.delete(id);
        return successMessage("Xoa phong tro thanh cong");
    }

    private PhongTro toEntity(CreatePhongTroRequest request) {
        PhongTro phongTro = new PhongTro();
        phongTro.setChuTro(toChuTro(request.getChuTroId()));
        phongTro.setTenPhong(request.getTenPhong());
        phongTro.setLoaiPhong(request.getLoaiPhong());
        phongTro.setGiaThue(request.getGiaThue());
        phongTro.setSucChua(request.getSucChua());
        phongTro.setMoTa(request.getMoTa());
        phongTro.setTrangThai(request.getTrangThai());
        return phongTro;
    }

    private PhongTro toEntity(UpdatePhongTroRequest request) {
        PhongTro phongTro = new PhongTro();
        phongTro.setChuTro(toChuTro(request.getChuTroId()));
        phongTro.setTenPhong(request.getTenPhong());
        phongTro.setLoaiPhong(request.getLoaiPhong());
        phongTro.setGiaThue(request.getGiaThue());
        phongTro.setSucChua(request.getSucChua());
        phongTro.setMoTa(request.getMoTa());
        phongTro.setTrangThai(request.getTrangThai());
        return phongTro;
    }

    private ChuTro toChuTro(Long chuTroId) {
        if (chuTroId == null) {
            return null;
        }
        ChuTro chuTro = new ChuTro();
        chuTro.setChuTroId(chuTroId);
        return chuTro;
    }
}
