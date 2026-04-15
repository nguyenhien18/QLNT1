package com.quanlynhatro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class PhongDichVuId implements Serializable {

    @Column(name = "phong_tro_id")
    private Long phongTroId;

    @Column(name = "dich_vu_id")
    private Long dichVuId;
}



