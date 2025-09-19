package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ejemplo de uso del nuevo sistema de fechaMaxima
 */
class EjemploFechaMaximaTest {

    private TareaValidator tareaValidator;

    @BeforeEach
    void setUp() {
        tareaValidator = new TareaValidator();
    }

    @Test
    void ejemploTareaDiariaConFechaMaxima() {
        // Crear una tarea diaria que se repita durante 2 semanas
        LocalDate fechaMaxima = LocalDate.now().plusWeeks(2);
        
        CrearTareaRequest request = new CrearTareaRequest(
                "Revisar emails", 
                1L, 
                2L, 
                TipoRecurrencia.DIARIA.name(), 
                null, 
                null, 
                null, 
                null, // Sin especificar número de repeticiones
                fechaMaxima // Se usará la fecha máxima en su lugar
        );

        TareaValida result = tareaValidator.validarTareaRequest(request);

        assertNotNull(result);
        assertEquals(TipoRecurrencia.DIARIA, result.tipoRecurrencia());
        assertEquals(fechaMaxima, result.fechaMaxima());
        // Cuando se usa fechaMaxima, numeroRepeticiones sigue siendo el valor por defecto
        assertEquals(90, result.numeroRepeticiones());
    }

    @Test
    void ejemploTareaSemanalConFechaMaxima() {
        // Crear una tarea semanal que se repita durante 3 meses
        LocalDate fechaMaxima = LocalDate.now().plusMonths(3);
        
        CrearTareaRequest request = new CrearTareaRequest(
                "Reunión de equipo", 
                1L, 
                2L, 
                TipoRecurrencia.SEMANAL.name(), 
                DiaSemana.LUNES.name(), 
                null, 
                null, 
                null, // Sin especificar número de repeticiones
                fechaMaxima // Se usará la fecha máxima en su lugar
        );

        TareaValida result = tareaValidator.validarTareaRequest(request);

        assertNotNull(result);
        assertEquals(TipoRecurrencia.SEMANAL, result.tipoRecurrencia());
        assertEquals(DiaSemana.LUNES, result.diaSemana());
        assertEquals(fechaMaxima, result.fechaMaxima());
        assertEquals(52, result.numeroRepeticiones()); // Valor por defecto
    }

    @Test
    void ejemploTareaConNumeroRepeticionesSinFechaMaxima() {
        // Crear una tarea con número específico de repeticiones (forma tradicional)
        CrearTareaRequest request = new CrearTareaRequest(
                "Backup de datos", 
                1L, 
                2L, 
                TipoRecurrencia.SEMANAL.name(), 
                DiaSemana.DOMINGO.name(), 
                null, 
                null, 
                10, // 10 repeticiones específicas
                null // Sin fecha máxima
        );

        TareaValida result = tareaValidator.validarTareaRequest(request);

        assertNotNull(result);
        assertEquals(TipoRecurrencia.SEMANAL, result.tipoRecurrencia());
        assertEquals(DiaSemana.DOMINGO, result.diaSemana());
        assertEquals(10, result.numeroRepeticiones());
        assertNull(result.fechaMaxima());
    }
}