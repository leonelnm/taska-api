package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
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
    public TareaEntity componerTareaPadre(TareaValida tareaValidada, PuestoEntity puesto, TurnoEntity turno) {
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
        // Si es UNA_VEZ, no generar tareas hijas
        if (tareaValidada.tipoRecurrencia() == TipoRecurrencia.UNA_VEZ) {
            return List.of();
        }

        LocalDate fechaInicio = tareaPadre.getFecha();
        List<TareaEntity> tareasHijas = new ArrayList<>();

        // Decidir si usar fechaMaxima o numeroRepeticiones
        if (tareaValidada.fechaMaxima() != null) {
            // Generar tareas hasta fechaMaxima
            generarTareasHastaFecha(tareasHijas, tareaPadre, tareaValidada, fechaInicio);
        } else {
            // Generar según numeroRepeticiones (solo si hay más de una repetición)
            if (tareaValidada.numeroRepeticiones() > 1) {
                generarTareasPorRepeticiones(tareasHijas, tareaPadre, tareaValidada, fechaInicio);
            }
        }

        return tareasHijas;
    }

    private void generarTareasHastaFecha(List<TareaEntity> tareasHijas, TareaEntity tareaPadre, 
                                         TareaValida tareaValidada, LocalDate fechaInicio) {
        LocalDate fechaMaxima = tareaValidada.fechaMaxima();
        int repeticion = 1;
        
        while (true) {
            LocalDate fechaTarea = calcularFechaPorRepeticion(fechaInicio, tareaValidada.tipoRecurrencia(), repeticion);
            
            // Si la fecha calculada supera la fecha máxima, detener
            if (fechaTarea.isAfter(fechaMaxima)) {
                break;
            }
            
            TareaEntity tareaHija = crearTareaBase(tareaValidada, tareaPadre.getPuesto(), tareaPadre.getTurno(), fechaTarea);
            tareaHija.setIdTareaPadre(tareaPadre.getId());
            tareasHijas.add(tareaHija);
            
            repeticion++;
            
            // Límite de seguridad para evitar bucles infinitos
            if (repeticion > 365) {
                break;
            }
        }
    }

    private void generarTareasPorRepeticiones(List<TareaEntity> tareasHijas, TareaEntity tareaPadre, 
                                             TareaValida tareaValidada, LocalDate fechaInicio) {
        // Generar desde la segunda repetición (índice 1) en adelante
        for (int i = 1; i < tareaValidada.numeroRepeticiones(); i++) {
            LocalDate fechaTarea = calcularFechaPorRepeticion(fechaInicio, tareaValidada.tipoRecurrencia(), i);
            
            TareaEntity tareaHija = crearTareaBase(tareaValidada, tareaPadre.getPuesto(), tareaPadre.getTurno(), fechaTarea);
            tareaHija.setIdTareaPadre(tareaPadre.getId());
            
            tareasHijas.add(tareaHija);
        }
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
            } catch (DateTimeException e) {
                // Si el día no existe en este mes, ir al siguiente mes
                return getSiguienteMes(fecha, diaMes);
            }
        } else {
            return getSiguienteMes(fecha, diaMes);
        }
    }

    private LocalDate getSiguienteMes(LocalDate fecha, int diaMes) {
        LocalDate siguienteMes = fecha.plusMonths(1);
        int ultimoDiaSiguienteMes = siguienteMes.lengthOfMonth();
        int diaAjustado = Math.min(diaMes, ultimoDiaSiguienteMes);
        return siguienteMes.withDayOfMonth(diaAjustado);
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