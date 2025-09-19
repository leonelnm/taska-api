package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.exception.AppValidationException;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TareaValidatorFechaMaximaTest {

    private TareaValidator tareaValidator;

    @BeforeEach
    void setUp() {
        tareaValidator = new TareaValidator();
    }

    @Test
    void validarFechaMaxima_ValidaCorrectamente() {
        LocalDate fechaMaxima = LocalDate.now().plusMonths(6);
        CrearTareaRequest request = new CrearTareaRequest(
                "Descripción de la tarea", 
                1L, 
                2L, 
                TipoRecurrencia.SEMANAL.name(), 
                "LUNES", 
                null, 
                null, 
                null,
                fechaMaxima
        );

        TareaValida result = tareaValidator.validarTareaRequest(request);

        assertNotNull(result);
        assertEquals(fechaMaxima, result.fechaMaxima());
    }

    @Test
    void validarFechaMaxima_NoRequeridaParaUnaVez() {
        LocalDate fechaInicio = LocalDate.now().plusDays(1);
        LocalDate fechaMaxima = LocalDate.now().plusMonths(6);
        
        CrearTareaRequest request = new CrearTareaRequest(
                "Descripción de la tarea", 
                1L, 
                2L, 
                TipoRecurrencia.UNA_VEZ.name(), 
                null, 
                null, 
                fechaInicio, 
                null,
                fechaMaxima
        );

        TareaValida result = tareaValidator.validarTareaRequest(request);

        assertNotNull(result);
        assertNull(result.fechaMaxima());
    }

    @Test
    void validarFechaMaxima_SuperaUnAnio_LanzaExcepcion() {
        LocalDate fechaMaxima = LocalDate.now().plusYears(1).plusDays(1);
        CrearTareaRequest request = new CrearTareaRequest(
                "Descripción de la tarea", 
                1L, 
                2L, 
                TipoRecurrencia.SEMANAL.name(), 
                "LUNES", 
                null, 
                null, 
                null,
                fechaMaxima
        );

        AppValidationException exception = assertThrows(AppValidationException.class, 
                () -> tareaValidator.validarTareaRequest(request));
        
        // Verificar que se lanza la excepción correcta
        assertTrue(exception.getErrors().containsKey("fechaMaxima"));
    }

    @Test
    void validarFechaMaxima_FechaPasada_LanzaExcepcion() {
        LocalDate fechaMaxima = LocalDate.now().minusDays(1);
        CrearTareaRequest request = new CrearTareaRequest(
                "Descripción de la tarea", 
                1L, 
                2L, 
                TipoRecurrencia.SEMANAL.name(), 
                "LUNES", 
                null, 
                null, 
                null,
                fechaMaxima
        );

        AppValidationException exception = assertThrows(AppValidationException.class, 
                () -> tareaValidator.validarTareaRequest(request));
        
        // Verificar que se lanza la excepción correcta
        assertTrue(exception.getErrors().containsKey("fechaMaxima"));
    }

    @Test
    void validarFechaMaxima_Null_EsValida() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Descripción de la tarea", 
                1L, 
                2L, 
                TipoRecurrencia.SEMANAL.name(), 
                "LUNES", 
                null, 
                null, 
                null,
                null
        );

        TareaValida result = tareaValidator.validarTareaRequest(request);

        assertNotNull(result);
        assertNull(result.fechaMaxima());
    }
}