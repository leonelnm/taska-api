package com.codigozerocuatro.taska.domain.service.impl;

import com.codigozerocuatro.taska.domain.exception.AppEntityNotFoundException;
import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.domain.service.PuestoService;
import com.codigozerocuatro.taska.domain.service.SecurityUtils;
import com.codigozerocuatro.taska.domain.service.TareaRecurrenciaGenerator;
import com.codigozerocuatro.taska.domain.service.TareaService;
import com.codigozerocuatro.taska.domain.service.TareaValidator;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import com.codigozerocuatro.taska.infra.dto.FiltroTareaRequest;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.TareaJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.repository.TurnoJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.specification.TareaSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService {

    private final PuestoService puestoService;
    private final TareaJpaRepository tareaRepository;
    private final TurnoJpaRepository turnoRepository;
    private final TareaValidator validator;
    private final SecurityUtils securityUtils;
    private final TareaRecurrenciaGenerator recurrenciaGenerator;

    @Override
    public TareaEntity crear(CrearTareaRequest request) {
        TareaValida tareaValidada = validator.validarTareaRequest(request);
        
        // Obtener entidades necesarias
        PuestoEntity puesto = puestoService.obtenerPuestoPorId(tareaValidada.puestoId());
        TurnoEntity turno = turnoRepository.findById(tareaValidada.turnoId())
                .orElseThrow(() -> new AppEntityNotFoundException(tareaValidada.turnoId()));
        
        // 1. Crear y persistir la tarea padre
        TareaEntity tareaPadre = recurrenciaGenerator.componerTareaPadre(tareaValidada, puesto, turno);
        TareaEntity tareaPadreGuardada = tareaRepository.save(tareaPadre);
        
        // 2. Generar y persistir las tareas hijas (solo si hay más de una repetición)
        List<TareaEntity> tareasHijas = recurrenciaGenerator.generarTareasHijas(tareaPadreGuardada, tareaValidada);
        if (!tareasHijas.isEmpty()) {
            tareaRepository.saveAll(tareasHijas);
        }

        // Devolver la tarea padre para mantener compatibilidad con la API
        return tareaPadreGuardada;
    }

    @Override
    public List<TareaEntity> todas() {
        return tareaRepository.findAll();
    }

    @Override
    public List<TareaEntity> buscar(FiltroTareaRequest filtro) {

        Long puestoId = getPuestoByUser(filtro);

        TipoRecurrencia tipoRecurrencia = StringUtils.isEmpty(filtro.tipoRecurrencia())
                ? null
                : validator.getTipoRecurrencia(filtro.tipoRecurrencia());

        DiaSemana diaSemana = StringUtils.isEmpty(filtro.diaSemana())
                ? null
                : validator.getDiaSemana(filtro.diaSemana());

        // Validar rango de fechas
        validator.validarRangoFechas(filtro.fechaInicio(), filtro.fechaFin());

        // Determinar qué specification de fecha usar
        Specification<TareaEntity> fechaSpec;
        if (filtro.fechaInicio() != null || filtro.fechaFin() != null) {
            // Usar búsqueda por rango
            fechaSpec = TareaSpecification.fechaBetween(filtro.fechaInicio(), filtro.fechaFin());
        } else {
            // Usar búsqueda por fecha específica (comportamiento original)
            fechaSpec = TareaSpecification.fechaIs(filtro.fecha());
        }

        Specification<TareaEntity> spec = Specification.allOf(
                List.of(
                        TareaSpecification.puestoEquals(puestoId),
                        TareaSpecification.turnoEquals(filtro.turnoId()),
                        TareaSpecification.diaSemanaEquals(diaSemana),
                        TareaSpecification.tipoRecurrenciaEquals(tipoRecurrencia),
                        TareaSpecification.isCompletadaEquals(filtro.completada()),
                        fechaSpec,
                        TareaSpecification.orderByFechaAsc()
                )
        );

        return tareaRepository.findAll(spec);
    }

    private Long getPuestoByUser(FiltroTareaRequest filtro) {
        UserEntity user = securityUtils.getCurrentAuthenticatedUser();

        if (user.isAdmin() || PuestoEnum.ENCARGADO.equals(user.getPuesto())){
            return filtro.puestoId();
        }

        PuestoEntity puesto = puestoService.obtenerPuestoPorNombre(user.getPuesto());
        return puesto.getId();
    }

    @Override
    public TareaEntity completar(Long id) {
        TareaEntity tarea = findById(id);
        boolean isCompletada = tarea.isCompletada();
        tarea.setCompletada(!isCompletada);
        tarea.setFechaCompletada(!isCompletada ? Instant.now() : null);
        return tareaRepository.save(tarea);
    }

    @Transactional
    @Override
    public List<TareaEntity> crearTodas(List<CrearTareaRequest> requests) {
        return requests.stream().map(this::crear).toList();
    }

    @Override
    public List<TareaEntity> obtenerSerieRecurrente(Long idTareaPadre) {
        return tareaRepository.findSerieRecurrente(idTareaPadre);
    }

    @Override
    public List<TareaEntity> obtenerTareasPorSemana(LocalDate fecha) {
        // Calcular el inicio de la semana (lunes)
        LocalDate inicioSemana = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Calcular el fin de la semana (domingo)
        LocalDate finSemana = fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        return tareaRepository.findByFechaBetweenOrderByFechaAsc(inicioSemana, finSemana);
    }

    /**
     * Elimina solo una tarea. Si es padre, actualiza las hijas y la siguiente por fecha será la nueva padre.
     */
    @Override
    @Transactional
    public void eliminarSoloTarea(Long id) {
        TareaEntity tarea = findById(id);
        // Si es tarea hija, simplemente se elimina
        if (tarea.getIdTareaPadre() != null) {
            tareaRepository.delete(tarea);
            return;
        }
        // Es tarea padre
        List<TareaEntity> hijas = tareaRepository.findByIdTareaPadre(id);
        if (hijas.isEmpty()) {
            // No hay hijas, solo eliminar la tarea padre
            tareaRepository.delete(tarea);
            return;
        }
        // Ordenar hijas por fecha ascendente y obtener la primera
        TareaEntity nuevaPadre = hijas.stream().min(Comparator.comparing(TareaEntity::getFecha))
                .orElseThrow(); // Esto nunca debería pasar, ya que hijas no está vacío

        nuevaPadre.setIdTareaPadre(null); // Ahora es padre
        // Actualizar las demás hijas para que apunten a la nueva padre
        hijas.stream()
                .filter(hija -> !hija.equals(nuevaPadre))
                .forEach(hija -> hija.setIdTareaPadre(nuevaPadre.getId()));

        // Persistir cambios
        tareaRepository.save(nuevaPadre);
        if (hijas.size() > 1) {
            tareaRepository.saveAll(hijas.stream().filter(hija -> !hija.equals(nuevaPadre)).toList());
        }
        // Eliminar la tarea padre original
        tareaRepository.delete(tarea);
    }

    /**
     * Elimina una tarea y todas las posteriores en la serie recurrente.
     */
    @Override
    @Transactional
    public void eliminarTareaYPosteriores(Long id) {
        TareaEntity tarea = findById(id);
        
        // Determinar el ID de la tarea padre de la serie
        Long idTareaPadre = tarea.getIdTareaPadre() != null ? tarea.getIdTareaPadre() : id;
        
        // Obtener solo las tareas hijas posteriores (el query ya filtra por idTareaPadre)
        List<TareaEntity> tareasHijas = tareaRepository.findTareasPosteriores(idTareaPadre, tarea.getFecha());
        
        // Eliminar todas las hijas posteriores
        if (!tareasHijas.isEmpty()) {
            tareaRepository.deleteAll(tareasHijas);
        }
        
        // Eliminar la tarea actual (ya sea padre o hija)
        tareaRepository.delete(tarea);
    }

    private TareaEntity findById(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new AppEntityNotFoundException(id));
    }
}
