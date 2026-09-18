package com.quanlynhatro.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PhongDichVuId implements Serializable {

    @Column(name = "phong_tro_id")
    private Long phongTroId;

    @Column(name = "dich_vu_id")
    private Long dichVuId;
}
