package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TareaValidatorRecurrenciaTest {

    private TareaValidator tareaValidator;

    @BeforeEach
    public void setUp() {
        tareaValidator = new TareaValidator();
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaDiariaUsaValorPorDefecto() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea diaria", 1L, 2L, 
                TipoRecurrencia.DIARIA.name(), 
                null, null, null, null, null
        );
        
        var tareaValida = tareaValidator.validarTareaRequest(request);
        
        assertEquals(90, tareaValida.numeroRepeticiones()); // Valor por defecto para DIARIA
        assertEquals(TipoRecurrencia.DIARIA, tareaValida.tipoRecurrencia());
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaSemanalUsaValorPorDefecto() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea semanal", 1L, 2L, 
                TipoRecurrencia.SEMANAL.name(), 
                DiaSemana.LUNES.name(), null, null, null, null
        );
        
        var tareaValida = tareaValidator.validarTareaRequest(request);
        
        assertEquals(52, tareaValida.numeroRepeticiones()); // Valor por defecto para SEMANAL
        assertEquals(DiaSemana.LUNES, tareaValida.diaSemana());
    }

    @Test
    public void testValidarTareaRequestConRecurrenciaMensualUsaValorPorDefecto() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea mensual", 1L, 2L, 
                TipoRecurrencia.MENSUAL.name(), 
                null, 15, null, null, null
        );
        
        var tareaValida = tareaValidator.validarTareaRequest(request);
        
        assertEquals(12, tareaValida.numeroRepeticiones()); // Valor por defecto para MENSUAL
        assertEquals(15, tareaValida.diaMes());
    }

    @Test
    public void testValidarTareaRequestConUnaVezSiempreEs1() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea única", 1L, 2L, 
                TipoRecurrencia.UNA_VEZ.name(), 
                null, null, LocalDate.now().plusDays(1), 999, null
        );
        
        var tareaValida = tareaValidator.validarTareaRequest(request);
        
        assertEquals(1, tareaValida.numeroRepeticiones()); // Siempre 1 para UNA_VEZ
    }

    @Test
    public void testValidarTareaRequestConNumeroRepeticionesPersonalizado() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea personalizada", 1L, 2L, 
                TipoRecurrencia.SEMANAL.name(), 
                DiaSemana.VIERNES.name(), null, null, 10, null
        );
        
        var tareaValida = tareaValidator.validarTareaRequest(request);
        
        assertEquals(10, tareaValida.numeroRepeticiones()); // Valor personalizado
    }

    @Test
    public void testValidarTareaRequestConNumeroRepeticionesExcesivoLanzaExcepcion() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea excesiva", 1L, 2L, 
                TipoRecurrencia.DIARIA.name(), 
                null, null, null, 500, null // Más de 365
        );
        
        assertThrows(Exception.class, () -> tareaValidator.validarTareaRequest(request));
    }

    @Test
    public void testValidarTareaRequestConNumeroRepeticionesCeroLanzaExcepcion() {
        CrearTareaRequest request = new CrearTareaRequest(
                "Tarea sin repeticiones", 1L, 2L, 
                TipoRecurrencia.DIARIA.name(), 
                null, null, null, 0, null
        );
        
        assertThrows(Exception.class, () -> tareaValidator.validarTareaRequest(request));
    }
}