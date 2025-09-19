package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TareaRecurrenciaGeneratorFechaMaximaTest {

    private TareaRecurrenciaGenerator generator;
    private PuestoEntity puesto;
    private TurnoEntity turno;

    @BeforeEach
    void setUp() {
        generator = new TareaRecurrenciaGenerator();
        puesto = new PuestoEntity();
        puesto.setId(1L);
        turno = new TurnoEntity();
        turno.setId(1L);
    }

    @Test
    void generarTareasHastaFechaMaxima_TareaSemanal() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaMaxima = hoy.plusWeeks(4); // 4 semanas
        
        TareaValida tareaValidada = new TareaValida(
                "Tarea semanal",
                1L,
                1L,
                TipoRecurrencia.SEMANAL,
                DiaSemana.LUNES,
                null,
                null,
                null,
                fechaMaxima
        );

        // Crear tarea padre
        TareaEntity tareaPadre = generator.componerTareaPadre(tareaValidada, puesto, turno);
        tareaPadre.setId(1L);

        // Generar tareas hijas
        List<TareaEntity> tareasHijas = generator.generarTareasHijas(tareaPadre, tareaValidada);

        // Deberían generarse aproximadamente 3-4 tareas hijas (sin contar la padre)
        assertTrue(tareasHijas.size() >= 3 && tareasHijas.size() <= 4);
        
        // Verificar que todas las fechas están dentro del rango
        for (TareaEntity tarea : tareasHijas) {
            assertTrue(tarea.getFecha().isAfter(tareaPadre.getFecha()));
            assertTrue(tarea.getFecha().isBefore(fechaMaxima) || tarea.getFecha().isEqual(fechaMaxima));
            assertEquals(1L, tarea.getIdTareaPadre());
        }
    }

    @Test
    void generarTareasHastaFechaMaxima_TareaDiaria() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaMaxima = hoy.plusDays(7); // 1 semana
        
        TareaValida tareaValidada = new TareaValida(
                "Tarea diaria",
                1L,
                1L,
                TipoRecurrencia.DIARIA,
                null,
                null,
                null,
                null,
                fechaMaxima
        );

        // Crear tarea padre
        TareaEntity tareaPadre = generator.componerTareaPadre(tareaValidada, puesto, turno);
        tareaPadre.setId(1L);

        // Generar tareas hijas
        List<TareaEntity> tareasHijas = generator.generarTareasHijas(tareaPadre, tareaValidada);

        // Deberían generarse 6-7 tareas hijas (7 días menos el padre)
        assertTrue(tareasHijas.size() >= 6 && tareasHijas.size() <= 7);
        
        // Verificar que todas las fechas están dentro del rango
        for (TareaEntity tarea : tareasHijas) {
            assertTrue(tarea.getFecha().isAfter(tareaPadre.getFecha()));
            assertTrue(tarea.getFecha().isBefore(fechaMaxima) || tarea.getFecha().isEqual(fechaMaxima));
            assertEquals(1L, tarea.getIdTareaPadre());
        }
    }

    @Test
    void generarTareasHastaFechaMaxima_UnaVez_NoGeneraTareasHijas() {
        LocalDate fechaInicio = LocalDate.now().plusDays(1);
        LocalDate fechaMaxima = LocalDate.now().plusMonths(1);
        
        TareaValida tareaValidada = new TareaValida(
                "Tarea única",
                1L,
                1L,
                TipoRecurrencia.UNA_VEZ,
                null,
                null,
                fechaInicio,
                1,
                fechaMaxima
        );

        // Crear tarea padre
        TareaEntity tareaPadre = generator.componerTareaPadre(tareaValidada, puesto, turno);
        tareaPadre.setId(1L);

        // Generar tareas hijas
        List<TareaEntity> tareasHijas = generator.generarTareasHijas(tareaPadre, tareaValidada);

        // No debería generar tareas hijas para UNA_VEZ
        assertTrue(tareasHijas.isEmpty());
    }

    @Test
    void generarTareasPorRepeticiones_CuandoNoHayFechaMaxima() {
        TareaValida tareaValidada = new TareaValida(
                "Tarea con repeticiones",
                1L,
                1L,
                TipoRecurrencia.SEMANAL,
                DiaSemana.LUNES,
                null,
                null,
                3, // 3 repeticiones
                null // Sin fecha máxima
        );

        // Crear tarea padre
        TareaEntity tareaPadre = generator.componerTareaPadre(tareaValidada, puesto, turno);
        tareaPadre.setId(1L);

        // Generar tareas hijas
        List<TareaEntity> tareasHijas = generator.generarTareasHijas(tareaPadre, tareaValidada);

        // Deberían generarse exactamente 2 tareas hijas (3 total - 1 padre)
        assertEquals(2, tareasHijas.size());
        
        for (TareaEntity tarea : tareasHijas) {
            assertEquals(1L, tarea.getIdTareaPadre());
        }
    }

    @Test
    void generarTareasHastaFechaMaxima_LimiteDeSeguridad() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaMaxima = hoy.plusYears(2); // 2 años (debería limitarse a 365 repeticiones)
        
        TareaValida tareaValidada = new TareaValida(
                "Tarea diaria larga",
                1L,
                1L,
                TipoRecurrencia.DIARIA,
                null,
                null,
                null,
                null,
                fechaMaxima
        );

        // Crear tarea padre
        TareaEntity tareaPadre = generator.componerTareaPadre(tareaValidada, puesto, turno);
        tareaPadre.setId(1L);

        // Generar tareas hijas
        List<TareaEntity> tareasHijas = generator.generarTareasHijas(tareaPadre, tareaValidada);

        // Debería limitarse a 365 repeticiones máximo
        assertTrue(tareasHijas.size() <= 365);
    }
}