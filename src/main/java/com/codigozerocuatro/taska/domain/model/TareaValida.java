package com.codigozerocuatro.taska.domain.model;

import java.time.LocalDate;

public record TareaValida(
        String descripcion,
        Long puestoId,
        Long turnoId,
        TipoRecurrencia tipoRecurrencia,
        DiaSemana diaSemana,
        Integer diaMes,
        LocalDate fecha,
        Integer numeroRepeticiones,
        LocalDate fechaMaxima
) {}
