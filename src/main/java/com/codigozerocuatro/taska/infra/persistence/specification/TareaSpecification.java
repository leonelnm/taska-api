package com.codigozerocuatro.taska.infra.persistence.specification;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TareaSpecification {

    public static Specification<TareaEntity> idEquals(Long id) {
        return (root, query, cb)
                -> id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<TareaEntity> puestoEquals(Long puestoId) {
        return (root, query, cb)
                -> puestoId == null ? null : cb.equal(root.join("puesto").get("id"), puestoId);
    }

    public static Specification<TareaEntity> turnoEquals(Long turnoId) {
        return (root, query, cb)
                -> turnoId == null ? null : cb.equal(root.join("turno").get("id"), turnoId);
    }

    public static Specification<TareaEntity> diaSemanaEquals(DiaSemana diaSemana) {
        return (root, query, cb)
                -> diaSemana == null ? null : cb.equal(root.get("diaSemana"), diaSemana.name());
    }

    public static Specification<TareaEntity> tipoRecurrenciaEquals(TipoRecurrencia tipoRecurrencia) {
        return (root, query, cb)
                -> tipoRecurrencia == null ? null : cb.equal(root.get("tipoRecurrencia"), tipoRecurrencia.name());
    }

    public static Specification<TareaEntity> isCompletadaEquals(Boolean completada) {
        return (root, query, cb)
                -> completada == null ? null : cb.equal(root.get("completada"), completada);
    }

    public static Specification<TareaEntity> fechaIs(LocalDate fecha) {
        return (root, query, cb)
                -> fecha == null ? null : cb.equal(root.get("fecha"), fecha);
    }

}
