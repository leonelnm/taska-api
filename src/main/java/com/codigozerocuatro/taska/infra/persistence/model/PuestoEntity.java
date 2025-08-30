package com.codigozerocuatro.taska.infra.persistence.model;

import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puesto")
@Data
@NoArgsConstructor
public class PuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private PuestoEnum puesto;

    public PuestoEntity(PuestoEnum puesto) {
        this.puesto = puesto;
    }

}
