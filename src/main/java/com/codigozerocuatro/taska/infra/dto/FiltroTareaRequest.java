package com.codigozerocuatro.taska.infra.dto;

public record FiltroTareaRequest(
        Long turnoId,
        Long puestoId,
        String tipoRecurrencia,
        String diaSemana,
        Boolean completada
) {
}
