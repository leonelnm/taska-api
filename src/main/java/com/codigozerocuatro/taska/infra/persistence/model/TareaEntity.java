package com.codigozerocuatro.taska.infra.persistence.model;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tarea")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TareaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 3000)
    private String descripcion;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean completada;

    private Instant fechaCompletada;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecurrencia tipoRecurrencia;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemana;

    private Integer diaMes;

    @Column(name = "id_tarea_padre")
    private Long idTareaPadre;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "puesto_id")
    private PuestoEntity puesto;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "turno_id")
    private TurnoEntity turno;

}
