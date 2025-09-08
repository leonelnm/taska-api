package com.codigozerocuatro.taska.domain.service.impl;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.domain.service.PuestoService;
import com.codigozerocuatro.taska.domain.service.SecurityUtils;
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

    @Override
    public TareaEntity crear(CrearTareaRequest request) {
        TareaValida tareaValidada = validator.validarTareaRequest(request);
        return persistir(tareaValidada);
    }

    private TareaEntity persistir(TareaValida tareaValidada ) {
        PuestoEntity puesto = puestoService.obtenerPuestoPorId(tareaValidada.puestoId());
        TurnoEntity turno = turnoRepository.findById(tareaValidada.turnoId())
                .orElseThrow(() -> new EntityNotFoundException("Turno " + tareaValidada.turnoId() + " no encontrado"));

        TareaEntity tarea = new TareaEntity();
        tarea.setDescripcion(tareaValidada.descripcion());
        tarea.setTipoRecurrencia(tareaValidada.tipoRecurrencia());
        tarea.setDiaSemana(tareaValidada.diaSemana());
        tarea.setDiaMes(tareaValidada.diaMes());
        tarea.setFecha(tareaValidada.fecha());
        tarea.setPuesto(puesto);
        tarea.setTurno(turno);

        return tareaRepository.save(tarea);
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
                        TareaSpecification.isCompletadaEquals(filtro.completada()))
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

    private TareaEntity findById(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarea " + id + " no encontrada"));
    }
}
