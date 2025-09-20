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

    @Mock
    private TareaRecurrenciaGenerator recurrenciaGenerator;

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
                null,
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
                null,
                26,
                null
        );

        PuestoEntity puesto = mock(PuestoEntity.class);
        TurnoEntity turno = mock(TurnoEntity.class);
        
        TareaEntity tareaPadre = new TareaEntity();
        tareaPadre.setDescripcion("descripcion");
        tareaPadre.setTipoRecurrencia(TipoRecurrencia.QUINCENAL);
        tareaPadre.setDiaSemana(DiaSemana.MIERCOLES);
        
        TareaEntity tareaPadreGuardada = new TareaEntity();
        tareaPadreGuardada.setId(1L);
        tareaPadreGuardada.setDescripcion("descripcion");
        tareaPadreGuardada.setTipoRecurrencia(TipoRecurrencia.QUINCENAL);
        tareaPadreGuardada.setDiaSemana(DiaSemana.MIERCOLES);

        when(validator.validarTareaRequest(request)).thenReturn(tareaValidada);
        when(puestoService.obtenerPuestoPorId(anyLong())).thenReturn(puesto);
        when(turnoRepository.findById(anyLong())).thenReturn(Optional.of(turno));
        when(recurrenciaGenerator.componerTareaPadre(tareaValidada, puesto, turno)).thenReturn(tareaPadre);
        when(tareaRepository.save(tareaPadre)).thenReturn(tareaPadreGuardada);
        when(recurrenciaGenerator.generarTareasHijas(tareaPadreGuardada, tareaValidada)).thenReturn(List.of());

        TareaEntity tarea = tareaService.crear(request);

        assertNotNull(tarea);
        assertEquals("descripcion", tarea.getDescripcion());
        assertEquals(TipoRecurrencia.QUINCENAL, tarea.getTipoRecurrencia());
        assertEquals(DiaSemana.MIERCOLES, tarea.getDiaSemana());
        assertEquals(1L, tarea.getId());
    }

    @Test
    void testBuscarTareas() {
        // given
        FiltroTareaRequest filtro = new FiltroTareaRequest(1L, 1L, TipoRecurrencia.SEMANAL.name(), DiaSemana.LUNES.name(), false, null, null, null);

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
        CrearTareaRequest req1 = new CrearTareaRequest("tarea1", 1L, 1L, TipoRecurrencia.UNA_VEZ.name(), null, null, LocalDate.now().plusDays(1), null, null);
        CrearTareaRequest req2 = new CrearTareaRequest("tarea2", 1L, 1L, TipoRecurrencia.MENSUAL.name(), null, 5, null, null, null);

        TareaEntity tarea1 = new TareaEntity();
        tarea1.setId(1L);
        tarea1.setDescripcion("tarea1");
        
        TareaEntity tarea2 = new TareaEntity();
        tarea2.setId(2L);
        tarea2.setDescripcion("tarea2");

        TareaValida tareaValidada1 = new TareaValida("tarea1", 1L, 1L, TipoRecurrencia.UNA_VEZ, null, null, LocalDate.now().plusDays(1), 1, null);
        TareaValida tareaValidada2 = new TareaValida("tarea2", 1L, 1L, TipoRecurrencia.MENSUAL, null, 5, null, 12, null);

        when(validator.validarTareaRequest(req1)).thenReturn(tareaValidada1);
        when(validator.validarTareaRequest(req2)).thenReturn(tareaValidada2);
        when(puestoService.obtenerPuestoPorId(anyLong())).thenReturn(new PuestoEntity());
        when(turnoRepository.findById(anyLong())).thenReturn(Optional.of(new TurnoEntity()));
        
        // Mock genérico para el generador
        when(recurrenciaGenerator.componerTareaPadre(any(TareaValida.class), any(), any()))
                .thenReturn(tarea1, tarea2);
        when(recurrenciaGenerator.generarTareasHijas(any(TareaEntity.class), any(TareaValida.class)))
                .thenReturn(List.of());
        
        when(tareaRepository.save(any(TareaEntity.class)))
                .thenReturn(tarea1, tarea2);

        List<TareaEntity> tareas = tareaService.crearTodas(List.of(req1, req2));

        assertEquals(2, tareas.size());
        assertEquals("tarea1", tareas.get(0).getDescripcion());
        assertEquals("tarea2", tareas.get(1).getDescripcion());
    }

    @Test
    void testObtenerTareasPorSemana_DevuelveTodas() {
        // given
        LocalDate fechaEnLaSemana = LocalDate.of(2025, 9, 17); // Miércoles
        LocalDate lunes = LocalDate.of(2025, 9, 15);
        LocalDate domingo = LocalDate.of(2025, 9, 21);

        TareaEntity tarea1 = new TareaEntity();
        tarea1.setId(1L);
        tarea1.setDescripcion("Tarea Lunes");
        tarea1.setFecha(lunes);

        TareaEntity tarea2 = new TareaEntity();
        tarea2.setId(2L);
        tarea2.setDescripcion("Tarea Miércoles");
        tarea2.setFecha(fechaEnLaSemana);

        TareaEntity tarea3 = new TareaEntity();
        tarea3.setId(3L);
        tarea3.setDescripcion("Tarea Domingo");
        tarea3.setFecha(domingo);

        List<TareaEntity> tareasEsperadas = List.of(tarea1, tarea2, tarea3);

        when(tareaRepository.findByFechaBetweenOrderByFechaAsc(lunes, domingo))
                .thenReturn(tareasEsperadas);

        // when
        List<TareaEntity> tareas = tareaService.obtenerTareasPorSemana(fechaEnLaSemana);

        // then
        assertNotNull(tareas);
        assertEquals(3, tareas.size());
        assertEquals("Tarea Lunes", tareas.get(0).getDescripcion());
        assertEquals("Tarea Miércoles", tareas.get(1).getDescripcion());
        assertEquals("Tarea Domingo", tareas.get(2).getDescripcion());
        assertEquals(lunes, tareas.get(0).getFecha());
        assertEquals(fechaEnLaSemana, tareas.get(1).getFecha());
        assertEquals(domingo, tareas.get(2).getFecha());

        verify(tareaRepository).findByFechaBetweenOrderByFechaAsc(lunes, domingo);
    }

    @Test
    void testObtenerTareasPorSemana_ExcluyeTareasFueraDeLaSemana() {
        // given
        LocalDate fechaEnLaSemana = LocalDate.of(2025, 9, 19); // Viernes
        LocalDate lunes = LocalDate.of(2025, 9, 15);
        LocalDate domingo = LocalDate.of(2025, 9, 21);

        // Tareas dentro de la semana
        TareaEntity tareaViernes = new TareaEntity();
        tareaViernes.setId(1L);
        tareaViernes.setDescripcion("Tarea Viernes");
        tareaViernes.setFecha(fechaEnLaSemana);

        TareaEntity tareaSabado = new TareaEntity();
        tareaSabado.setId(2L);
        tareaSabado.setDescripcion("Tarea Sábado");
        tareaSabado.setFecha(LocalDate.of(2025, 9, 20));

        // Solo las tareas de la semana son devueltas por el repositorio
        List<TareaEntity> tareasEnLaSemana = List.of(tareaViernes, tareaSabado);

        when(tareaRepository.findByFechaBetweenOrderByFechaAsc(lunes, domingo))
                .thenReturn(tareasEnLaSemana);

        // when
        List<TareaEntity> tareas = tareaService.obtenerTareasPorSemana(fechaEnLaSemana);

        // then
        assertNotNull(tareas);
        assertEquals(2, tareas.size());
        assertEquals("Tarea Viernes", tareas.get(0).getDescripcion());
        assertEquals("Tarea Sábado", tareas.get(1).getDescripcion());

        // Verificar que se llamó con las fechas correctas de la semana
        verify(tareaRepository).findByFechaBetweenOrderByFechaAsc(lunes, domingo);

        // Verificar que las tareas están ordenadas por fecha
        assertTrue(tareas.get(0).getFecha().isBefore(tareas.get(1).getFecha()) ||
                   tareas.get(0).getFecha().isEqual(tareas.get(1).getFecha()));
    }
}
