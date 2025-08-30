package com.codigozerocuatro.taska.infra.persistence.model;

import com.codigozerocuatro.taska.domain.model.TurnoEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "turno")
@Data
@NoArgsConstructor
public class TurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private TurnoEnum turno;

    public TurnoEntity(TurnoEnum turno) {
        this.turno = turno;
    }
}
