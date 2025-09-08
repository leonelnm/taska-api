package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.RolEnum;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.domain.service.impl.TareaServiceImpl;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import com.codigozerocuatro.taska.infra.dto.FiltroTareaRequest;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.TareaJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.repository.TurnoJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TareaServiceTest {

    @Mock
    private PuestoService puestoService;

    @Mock
    private TareaJpaRepository tareaRepository;

    @Mock
    private TurnoJpaRepository turnoRepository;

    @Mock
    private TareaValidator validator;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TareaServiceImpl tareaService;

    @Test
    public void testCrearTarea() {
        CrearTareaRequest request = new CrearTareaRequest(
                "descripcion",
                1L,
                1L,
                TipoRecurrencia.QUINCENAL.name(),
                DiaSemana.MIERCOLES.name(),
                null,
                null
        );

        TareaValida tareaValidada = new TareaValida(
                "descripcion",
                1L,
                1L,
                TipoRecurrencia.QUINCENAL,
                DiaSemana.MIERCOLES,
                null,
                null
        );

        PuestoEntity puesto = mock(PuestoEntity.class);
        TurnoEntity turno = mock(TurnoEntity.class);

        when(validator.validarTareaRequest(request)).thenReturn(tareaValidada);
        when(puestoService.obtenerPuestoPorId(anyLong())).thenReturn(puesto);
        when(turnoRepository.findById(anyLong())).thenReturn(Optional.of(turno));
        when(tareaRepository.save(any(TareaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TareaEntity tarea = tareaService.crear(request);

        assertNotNull(tarea);
        assertEquals("descripcion", tarea.getDescripcion());
        assertEquals(TipoRecurrencia.QUINCENAL, tarea.getTipoRecurrencia());
        assertEquals(DiaSemana.MIERCOLES, tarea.getDiaSemana());
    }

    @Test
    void testBuscarTareas() {
        // given
        FiltroTareaRequest filtro = new FiltroTareaRequest(1L, 1L, TipoRecurrencia.SEMANAL.name(), DiaSemana.LUNES.name(), false);

        UserEntity user = new UserEntity();
        user.setRol(RolEnum.ADMIN);
        when(securityUtils.getCurrentAuthenticatedUser()).thenReturn(user);

        TareaEntity tareaMock = new TareaEntity();
        tareaMock.setDescripcion("Tarea de prueba");

        when(tareaRepository.findAll(any(Specification.class))).thenReturn(List.of(tareaMock));

        // when
        List<TareaEntity> tareas = tareaService.buscar(filtro);

        // then
        assertNotNull(tareas);
        assertEquals(1, tareas.size());
        assertEquals("Tarea de prueba", tareas.getFirst().getDescripcion());
    }

    @Test
    void testCompletarTarea() {
        // given
        Long id = 1L;
        TareaEntity tarea = new TareaEntity();
        tarea.setId(id);
        tarea.setCompletada(false);

        when(tareaRepository.findById(id)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(TareaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TareaEntity completadaTarea = tareaService.completar(id);

        // then
        assertNotNull(completadaTarea);
        assertTrue(completadaTarea.isCompletada());
        assertNotNull(completadaTarea.getFechaCompletada());
    }

    @Test
    void testCrearTodas() {
        CrearTareaRequest req1 = new CrearTareaRequest("tarea1", 1L, 1L, TipoRecurrencia.UNA_VEZ.name(), null, null, LocalDate.now().plusDays(1));
        CrearTareaRequest req2 = new CrearTareaRequest("tarea2", 1L, 1L, TipoRecurrencia.MENSUAL.name(), null, 5, null);

        TareaEntity tarea1 = new TareaEntity();
        tarea1.setDescripcion("tarea1");
        TareaEntity tarea2 = new TareaEntity();
        tarea2.setDescripcion("tarea2");

        when(validator.validarTareaRequest(any())).thenReturn(
                new TareaValida("tarea1", 1L, 1L, TipoRecurrencia.UNA_VEZ, null, null, LocalDate.now().plusDays(1)),
                new TareaValida("tarea2", 1L, 1L, TipoRecurrencia.MENSUAL, null, 5, null)
        );
        when(puestoService.obtenerPuestoPorId(anyLong())).thenReturn(new PuestoEntity());
        when(turnoRepository.findById(anyLong())).thenReturn(Optional.of(new TurnoEntity()));
        when(tareaRepository.save(any(TareaEntity.class)))
                .thenReturn(tarea1)
                .thenReturn(tarea2);

        List<TareaEntity> tareas = tareaService.crearTodas(List.of(req1, req2));

        assertEquals(2, tareas.size());
        assertEquals("tarea1", tareas.get(0).getDescripcion());
        assertEquals("tarea2", tareas.get(1).getDescripcion());
    }
}
