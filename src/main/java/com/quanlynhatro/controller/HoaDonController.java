package com.quanlynhatro.controller;

import lombok.RequiredArgsConstructor;
import com.quanlynhatro.dto.request.CreateHoaDonRequest;
import com.quanlynhatro.dto.request.UpdateHoaDonRequest;
import com.quanlynhatro.dto.response.ApiResponse;
import com.quanlynhatro.dto.response.HoaDonResponse;
import com.quanlynhatro.dto.response.InvoicePreviewResponse;
import com.quanlynhatro.entity.HoaDon;
import com.quanlynhatro.entity.HopDong;
import com.quanlynhatro.entity.PhongTro;
import com.quanlynhatro.exception.AppException;
import com.quanlynhatro.mapper.HoaDonMapper;
import com.quanlynhatro.service.HoaDonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController extends ApiControllerSupport {

    private final HoaDonService hoaDonService;
    private final HoaDonMapper hoaDonMapper;

    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                hoaDonService::getAll,
                () -> hoaDonService.getPage(page, size, sortBy, direction),
                hoaDonMapper::toResponse
        );
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false) String room,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String period,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        return success(hoaDonService.search(room, status, period, page, size).map(hoaDonMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ApiResponse<HoaDonResponse> getById(@PathVariable Long id) {
        return success(hoaDonMapper.toResponse(hoaDonService.getById(id)));
    }

    @GetMapping("/phong/{phongTroId}")
    public ApiResponse<?> getByPhongTro(@PathVariable Long phongTroId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> hoaDonService.getByPhongTroId(phongTroId),
                () -> hoaDonService.getPageByPhongTroId(phongTroId, page, size, sortBy, direction),
                hoaDonMapper::toResponse
        );
    }

    @GetMapping("/hop-dong/{hopDongId}")
    public ApiResponse<?> getByHopDong(@PathVariable Long hopDongId,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size,
                                       @RequestParam(required = false) String sortBy,
                                       @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> hoaDonService.getByHopDongId(hopDongId),
                () -> hoaDonService.getPageByHopDongId(hopDongId, page, size, sortBy, direction),
                hoaDonMapper::toResponse
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
                () -> hoaDonService.getByTrangThai(trangThai),
                () -> hoaDonService.getPageByTrangThai(trangThai, page, size, sortBy, direction),
                hoaDonMapper::toResponse
        );
    }

    @GetMapping("/ky")
    public ApiResponse<?> getByKyHoaDon(@RequestParam String kyHoaDon,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(required = false) String direction) {
        return pagedMapped(
                page, size, sortBy, direction,
                () -> hoaDonService.getByKyHoaDon(kyHoaDon),
                () -> hoaDonService.getPageByKyHoaDon(kyHoaDon, page, size, sortBy, direction),
                hoaDonMapper::toResponse
        );
    }

    @GetMapping("/preview")
    public ApiResponse<InvoicePreviewResponse> preview(@RequestParam(required = false) Long phongTroId,
                                                       @RequestParam(required = false) Long hopDongId,
                                                       @RequestParam String kyHoaDon) {
        if (hopDongId != null) {
            return success(hoaDonService.previewByContract(hopDongId, kyHoaDon));
        }
        if (phongTroId != null) {
            return success(hoaDonService.previewByRoom(phongTroId, kyHoaDon));
        }
        throw new AppException(HttpStatus.BAD_REQUEST, "Vui long cung cap phongTroId hoac hopDongId");
    }

    @PostMapping
    public ApiResponse<HoaDonResponse> create(@Valid @RequestBody CreateHoaDonRequest request) {
        return success(hoaDonMapper.toResponse(hoaDonService.createFromRequest(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<HoaDonResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateHoaDonRequest request) {
        return success(hoaDonMapper.toResponse(hoaDonService.update(id, toEntity(request))));
    }

    @PutMapping("/{id}/da-thanh-toan")
    public ApiResponse<HoaDonResponse> markAsPaid(@PathVariable Long id) {
        return success(hoaDonMapper.toResponse(hoaDonService.markAsPaid(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hoaDonService.delete(id);
        return successMessage("Xoa hoa don thanh cong");
    }

    private HoaDon toEntity(UpdateHoaDonRequest request) {
        HoaDon entity = new HoaDon();
        entity.setHopDong(toHopDong(request.getHopDongId()));
        entity.setPhongTro(toPhongTro(request.getPhongTroId()));
        entity.setNgayLap(request.getNgayLap());
        entity.setKyHoaDon(request.getKyHoaDon());
        entity.setTienPhong(request.getTienPhong());
        entity.setTienDien(request.getTienDien());
        entity.setTienNuoc(request.getTienNuoc());
        entity.setTienDichVu(request.getTienDichVu());
        entity.setTongTien(request.getTongTien());
        entity.setTrangThai(request.getTrangThai());
        return entity;
    }

    private HopDong toHopDong(Long id) {
        if (id == null) {
            return null;
        }
        HopDong hopDong = new HopDong();
        hopDong.setHopDongId(id);
        return hopDong;
    }

    private PhongTro toPhongTro(Long id) {
        if (id == null) {
            return null;
        }
        PhongTro phongTro = new PhongTro();
        phongTro.setPhongTroId(id);
        return phongTro;
    }
}
