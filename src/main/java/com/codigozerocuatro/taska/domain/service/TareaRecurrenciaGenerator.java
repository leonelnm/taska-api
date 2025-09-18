package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
public class TareaRecurrenciaGenerator {

    /**
     * Crea la tarea padre (primera tarea de la serie recurrente).
     * 
     * @param tareaValidada Datos validados de la tarea
     * @param puesto Entidad del puesto
     * @param turno Entidad del turno
     * @return Tarea padre (sin persistir)
     */
    public TareaEntity crearTareaPadre(TareaValida tareaValidada, PuestoEntity puesto, TurnoEntity turno) {
        LocalDate fechaInicio = obtenerFechaInicio(tareaValidada);
        return crearTareaBase(tareaValidada, puesto, turno, fechaInicio);
    }

    /**
     * Genera las tareas hijas basándose en una tarea padre ya persistida.
     * 
     * @param tareaPadre Tarea padre ya persistida con ID
     * @param tareaValidada Datos validados de la tarea original
     * @return Lista de tareas hijas (sin persistir)
     */
    public List<TareaEntity> generarTareasHijas(TareaEntity tareaPadre, TareaValida tareaValidada) {
        // Solo generar tareas hijas si hay más de una repetición
        if (tareaValidada.numeroRepeticiones() <= 1) {
            return List.of();
        }
        
        LocalDate fechaInicio = tareaPadre.getFecha();

        List<TareaEntity> tareasHijas = new ArrayList<>();
        // Generar desde la segunda repetición (índice 1) en adelante
        for (int i = 1; i < tareaValidada.numeroRepeticiones(); i++) {
            LocalDate fechaTarea = calcularFechaPorRepeticion(fechaInicio, tareaValidada.tipoRecurrencia(), i);
            
            TareaEntity tareaHija = crearTareaBase(tareaValidada, tareaPadre.getPuesto(), tareaPadre.getTurno(), fechaTarea);
            tareaHija.setIdTareaPadre(tareaPadre.getId());
            
            tareasHijas.add(tareaHija);
        }
        
        return tareasHijas;
    }

    private TareaEntity crearTareaBase(TareaValida tareaValidada, PuestoEntity puesto, TurnoEntity turno, LocalDate fecha) {
        TareaEntity tarea = new TareaEntity();
        tarea.setDescripcion(tareaValidada.descripcion());
        tarea.setTipoRecurrencia(tareaValidada.tipoRecurrencia());
        tarea.setDiaSemana(tareaValidada.diaSemana());
        tarea.setDiaMes(tareaValidada.diaMes());
        tarea.setFecha(fecha);
        tarea.setPuesto(puesto);
        tarea.setTurno(turno);
        return tarea;
    }

    private LocalDate obtenerFechaInicio(TareaValida tareaValidada) {
        // Para UNA_VEZ, usar la fecha especificada
        if (tareaValidada.tipoRecurrencia() == TipoRecurrencia.UNA_VEZ) {
            return tareaValidada.fecha();
        }
        
        LocalDate hoy = LocalDate.now();
        
        return switch (tareaValidada.tipoRecurrencia()) {
            case SEMANAL, QUINCENAL -> ajustarADiaDeLaSemana(hoy, tareaValidada.diaSemana());
            case MENSUAL -> ajustarADiaDelMes(hoy, tareaValidada.diaMes());
            default -> hoy;
        };
    }

    private LocalDate calcularFechaPorRepeticion(LocalDate fechaInicio, TipoRecurrencia tipoRecurrencia, int repeticion) {
        
        return switch (tipoRecurrencia) {
            case DIARIA -> fechaInicio.plusDays(repeticion);
            case SEMANAL -> fechaInicio.plusWeeks(repeticion);
            case QUINCENAL -> fechaInicio.plusWeeks(repeticion * 2L);
            case MENSUAL -> fechaInicio.plusMonths(repeticion);
            default -> fechaInicio;
        };
    }

    private LocalDate ajustarADiaDeLaSemana(LocalDate fecha, DiaSemana diaSemana) {
        DayOfWeek dayOfWeek = convertirDiaSemana(diaSemana);
        
        // Si ya es el día correcto, devolver la fecha
        if (fecha.getDayOfWeek() == dayOfWeek) {
            return fecha;
        }
        
        // Buscar el próximo día de la semana especificado
        return fecha.with(TemporalAdjusters.next(dayOfWeek));
    }

    private LocalDate ajustarADiaDelMes(LocalDate fecha, Integer diaMes) {
        // Si el día actual es menor o igual al día objetivo, usar este mes
        if (fecha.getDayOfMonth() <= diaMes) {
            try {
                return fecha.withDayOfMonth(diaMes);
            } catch (Exception e) {
                // Si el día no existe en este mes, ir al siguiente mes
                return fecha.plusMonths(1).withDayOfMonth(Math.min(diaMes, fecha.plusMonths(1).lengthOfMonth()));
            }
        } else {
            // Si ya pasó el día en este mes, ir al siguiente mes
            LocalDate siguienteMes = fecha.plusMonths(1);
            try {
                return siguienteMes.withDayOfMonth(diaMes);
            } catch (Exception e) {
                return siguienteMes.withDayOfMonth(Math.min(diaMes, siguienteMes.lengthOfMonth()));
            }
        }
    }

    private DayOfWeek convertirDiaSemana(DiaSemana diaSemana) {
        return switch (diaSemana) {
            case LUNES -> DayOfWeek.MONDAY;
            case MARTES -> DayOfWeek.TUESDAY;
            case MIERCOLES -> DayOfWeek.WEDNESDAY;
            case JUEVES -> DayOfWeek.THURSDAY;
            case VIERNES -> DayOfWeek.FRIDAY;
            case SABADO -> DayOfWeek.SATURDAY;
            case DOMINGO -> DayOfWeek.SUNDAY;
        };
    }
}