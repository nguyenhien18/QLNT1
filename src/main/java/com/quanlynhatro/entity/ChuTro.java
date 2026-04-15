package com.quanlynhatro.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chu_tro")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChuTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chu_tro_id")
    private Long chuTroId;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "sdt", unique = true, length = 20)
    private String sdt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "mat_khau", nullable = false, length = 255)
    private String matKhau;
}



