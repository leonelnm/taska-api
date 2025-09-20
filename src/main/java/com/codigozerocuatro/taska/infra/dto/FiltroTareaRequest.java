package com.codigozerocuatro.taska.infra.dto;

import java.time.LocalDate;

public record FiltroTareaRequest(
        Long turnoId,
        Long puestoId,
        String tipoRecurrencia,
        String diaSemana,
        Boolean completada,
        LocalDate fecha,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
