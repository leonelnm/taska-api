package com.codigozerocuatro.taska.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrearTareaRequest(
        @NotBlank(message = "La descripción no puede estar vacía") String descripcion,
        @NotNull(message = "El puesto es obligatorio") Long puestoId,
        @NotNull(message = "El turno es obligatorio") Long turnoId,
        @NotNull(message = "El tipo de recurrencia es obligatorio") String tipoRecurrencia,
        String diaSemana,
        Integer diaMes,
        LocalDate fecha
) {
}
