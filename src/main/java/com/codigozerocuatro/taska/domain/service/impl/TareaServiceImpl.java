package com.codigozerocuatro.taska.domain.service.impl;

import com.codigozerocuatro.taska.domain.exception.AppEntityNotFoundException;
import com.codigozerocuatro.taska.domain.model.DiaSemana;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

        Specification<TareaEntity> spec = Specification.allOf(
                List.of(
                        TareaSpecification.puestoEquals(puestoId),
                        TareaSpecification.turnoEquals(filtro.turnoId()),
                        TareaSpecification.diaSemanaEquals(diaSemana),
                        TareaSpecification.tipoRecurrenciaEquals(tipoRecurrencia),
                        TareaSpecification.isCompletadaEquals(filtro.completada()),
                        TareaSpecification.fechaIs(filtro.fecha())
                )
        );

        return tareaRepository.findAll(spec);
    }

    private Long getPuestoByUser(FiltroTareaRequest filtro) {
        UserEntity user = securityUtils.getCurrentAuthenticatedUser();

        if (user.isAdmin()){
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
    public void eliminarTareaYSusHijas(Long id) {
        TareaEntity tarea = findById(id);
        
        // Si es una tarea padre, eliminar todas las hijas
        if (tarea.getIdTareaPadre() == null) {
            List<TareaEntity> tareasHijas = tareaRepository.findByIdTareaPadre(id);
            tareaRepository.deleteAll(tareasHijas);
        }
        
        // Eliminar la tarea principal
        tareaRepository.delete(tarea);
    }

    @Override
    public void eliminarTareaYPosteriores(Long id) {
        List<TareaEntity> tareasPosteriores = findTareasPosteriores(id);

        // Eliminar todas las tareas posteriores (incluyendo la actual)
        tareaRepository.deleteAll(tareasPosteriores);
    }

    @Override
    public void actualizarTareaYPosteriores(Long id, String nuevaDescripcion) {
        List<TareaEntity> tareasPosteriores = findTareasPosteriores(id);

        // Actualizar la descripción de todas las tareas posteriores (incluyendo la actual)
        tareasPosteriores.forEach(t -> t.setDescripcion(nuevaDescripcion));
        tareaRepository.saveAll(tareasPosteriores);
    }

    private List<TareaEntity> findTareasPosteriores(Long id) {
        TareaEntity tarea = findById(id);

        // Obtener el ID de la tarea padre
        Long idTareaPadre = tarea.getIdTareaPadre() != null ? tarea.getIdTareaPadre() : id;

        // Obtener todas las tareas posteriores a esta fecha en la serie
        List<TareaEntity> tareasPosteriores = tareaRepository.findTareasPosteriores(idTareaPadre, tarea.getFecha());

        // Si la tarea actual es la padre, también buscar tareas posteriores que tengan como padre esta tarea
        if (tarea.getIdTareaPadre() == null) {
            List<TareaEntity> todasLasTareas = tareaRepository.findSerieRecurrente(id);
            tareasPosteriores = todasLasTareas.stream()
                    .filter(t -> !t.getFecha().isBefore(tarea.getFecha()))
                    .toList();
        }
        return tareasPosteriores;
    }

    @Override
    public List<TareaEntity> obtenerSerieRecurrente(Long idTareaPadre) {
        return tareaRepository.findSerieRecurrente(idTareaPadre);
    }

    private TareaEntity findById(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new AppEntityNotFoundException(id));
    }
}
