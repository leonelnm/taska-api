package com.codigozerocuatro.taska.infra.dto;

import com.codigozerocuatro.taska.domain.model.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrearTareaRequest(
        @NotBlank(message = ErrorCode.DESCRIPCION_REQUIRED) String descripcion,
        @NotNull(message = ErrorCode.PUESTO_REQUIRED) Long puestoId,
        @NotNull(message = ErrorCode.TURNO_REQUIRED) Long turnoId,
        @NotNull(message = ErrorCode.TIPO_RECURRENCIA_REQUIRED) String tipoRecurrencia,
        String diaSemana,
        Integer diaMes,
        LocalDate fecha,
        Integer numeroRepeticiones
) {
}
