package com.quanlynhatro.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phong_dich_vu")
public class PhongDichVu {

    @EmbeddedId
    private PhongDichVuId id;

    @ManyToOne
    @MapsId("phongTroId")
    @JoinColumn(name = "phong_tro_id", nullable = false)
    private PhongTro phongTro;

    @ManyToOne
    @MapsId("dichVuId")
    @JoinColumn(name = "dich_vu_id", nullable = false)
    private DichVu dichVu;
}
