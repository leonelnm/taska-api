package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TareaValidatorTest {

    private TareaValidator tareaValidator;

    @BeforeEach
    public void setUp() {
        tareaValidator = new TareaValidator();
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaUnicaYFechaVaciaLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.UNA_VEZ, null);
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaUnicaYFechaPasadaLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.UNA_VEZ, DiaSemana.LUNES.name());
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaSemanalYLunesComoDiaSemana() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.SEMANAL, "LUNES");
        TareaValida tareaValida = tareaValidator.validarTareaRequest(request);
        assertEquals(DiaSemana.LUNES, tareaValida.diaSemana());
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaMensualYDia15ComoDiaMes() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.MENSUAL, 15, null);
        TareaValida tareaValida = tareaValidator.validarTareaRequest(request);
        assertEquals(15, tareaValida.diaMes());
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaMensualYDia40ComoDiaMesLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.MENSUAL, 40, null);
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaMensualYDiaNegativoComoDiaMesLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.MENSUAL, -1, null);
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaQuincenalYLunesComoDiaSemana() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.QUINCENAL, "LUNES");
        TareaValida tareaValida = tareaValidator.validarTareaRequest(request);
        assertEquals(DiaSemana.LUNES, tareaValida.diaSemana());
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaQuincenalYDiaSemanaVacioLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.QUINCENAL, null);
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaQuincenalYDiaSemanaFakeLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.QUINCENAL, "FAKE");
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }


    @Test
    public void testValidarTareaRequestConRecurrenciaUnaVezYFechaPasadaLanzaExcepcion() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.UNA_VEZ, null, LocalDate.now().minusDays(1));
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaUnaVezYFechaHoy() {
        CrearTareaRequest request = crearCrearTareaRequest(TipoRecurrencia.UNA_VEZ, null, LocalDate.now());
        TareaValida tareaValida = tareaValidator.validarTareaRequest(request);
        assertEquals(TipoRecurrencia.UNA_VEZ, tareaValida.tipoRecurrencia());
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaFakeLanzaExcepcion() {
        CrearTareaRequest request = new CrearTareaRequest("Descripción de la tarea", 1L, 2L, "tipoRecurrencia.FAKE", null, null, null);
        assertThrows(ValidationException.class, () -> tareaValidator.validarTareaRequest(request));
    }

    private CrearTareaRequest crearCrearTareaRequest(TipoRecurrencia tipoRecurrencia, String diaSemana) {
        return new CrearTareaRequest("Descripción de la tarea", 1L, 2L, tipoRecurrencia.name(), diaSemana, null, null);
    }

    private CrearTareaRequest crearCrearTareaRequest(TipoRecurrencia tipoRecurrencia, Integer diaMes, LocalDate fecha) {
        return new CrearTareaRequest("Descripción de la tarea", 1L, 2L, tipoRecurrencia.name(), null, diaMes, fecha);
    }
}