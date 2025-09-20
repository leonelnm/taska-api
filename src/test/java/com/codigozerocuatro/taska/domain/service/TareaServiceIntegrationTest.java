package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.exception.AppValidationException;
import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.domain.model.RolEnum;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.domain.model.TurnoEnum;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import com.codigozerocuatro.taska.infra.dto.FiltroTareaRequest;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.PuestoJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.repository.TareaJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.repository.TurnoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TareaServiceIntegrationTest {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private TareaJpaRepository tareaRepository;

    @Autowired
    private PuestoJpaRepository puestoRepository;

    @Autowired
    private TurnoJpaRepository turnoRepository;

    @MockitoBean
    private SecurityUtils securityUtils;

    private PuestoEntity puesto;
    private TurnoEntity turno;
    private LocalDate fechaBaseSemana; // Semana donde están las 5 tareas principales

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        tareaRepository.deleteAll();
        puestoRepository.deleteAll();
        turnoRepository.deleteAll();

        // Crear datos base necesarios
        puesto = new PuestoEntity(PuestoEnum.COCINERO);
        puesto = puestoRepository.save(puesto);

        turno = new TurnoEntity(TurnoEnum.MANANA);
        turno = turnoRepository.save(turno);

        // Configurar fecha base para la semana objetivo
        fechaBaseSemana = LocalDate.now().plusDays(30).with(java.time.DayOfWeek.MONDAY);

        // Crear usuario admin para los tests de búsqueda
        UserEntity adminUser = new UserEntity();
        adminUser.setUsername("admin");
        adminUser.setRol(RolEnum.ADMIN);
        adminUser.setPuesto(PuestoEnum.ENCARGADO);

        // Configurar mock de SecurityUtils para devolver el usuario admin
        when(securityUtils.getCurrentAuthenticatedUser()).thenReturn(adminUser);

        // Crear 10 tareas usando el servicio
        crearTareasIniciales();
    }

    private void crearTareasIniciales() {
        // Usar la fecha base configurada en setUp
        LocalDate lunes = fechaBaseSemana;
        LocalDate martes = lunes.plusDays(1);
        LocalDate miercoles = lunes.plusDays(2);
        LocalDate viernes = lunes.plusDays(4);
        LocalDate domingo = lunes.plusDays(6);
        
        // Tareas de la semana objetivo
        tareaService.crear(new CrearTareaRequest("Tarea Lunes", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, lunes, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Martes", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, martes, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Miércoles", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, miercoles, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Viernes", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, viernes, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Domingo", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, domingo, 1, null));

        // Tareas fuera de la semana objetivo
        LocalDate domingoAnterior = lunes.minusDays(1);
        LocalDate lunesPosterior = domingo.plusDays(1);
        LocalDate miercolesPosterior = lunesPosterior.plusDays(2);
        
        tareaService.crear(new CrearTareaRequest("Tarea Domingo Anterior", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, domingoAnterior, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Lunes Posterior", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, lunesPosterior, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Miércoles Posterior", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, miercolesPosterior, 1, null));

        // Tareas de semanas más lejanas
        LocalDate semanaAnterior = lunes.minusWeeks(1);
        LocalDate semanaPosterior = lunes.plusWeeks(2);
        
        tareaService.crear(new CrearTareaRequest("Tarea Semana Anterior", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, semanaAnterior, 1, null));
        
        tareaService.crear(new CrearTareaRequest("Tarea Semana Posterior", puesto.getId(), turno.getId(), 
            TipoRecurrencia.UNA_VEZ.name(), null, null, semanaPosterior, 1, null));
    }

    @Test
    void testObtenerTareasPorSemana_DevuelveTareasDeUnaSemanaCompleta() {
        // given - Las tareas ya están creadas en setUp()
        LocalDate fechaEnLaSemana = fechaBaseSemana.plusDays(2); // Miércoles de la semana objetivo
        
        // when
        List<TareaEntity> tareasObtenidas = tareaService.obtenerTareasPorSemana(fechaEnLaSemana);

        // then
        assertNotNull(tareasObtenidas);
        assertEquals(5, tareasObtenidas.size(), "Debe devolver exactamente 5 tareas de la semana objetivo");

        // Verificar que están ordenadas por fecha y son las correctas
        assertEquals("Tarea Lunes", tareasObtenidas.get(0).getDescripcion());
        assertEquals("Tarea Martes", tareasObtenidas.get(1).getDescripcion());
        assertEquals("Tarea Miércoles", tareasObtenidas.get(2).getDescripcion());
        assertEquals("Tarea Viernes", tareasObtenidas.get(3).getDescripcion());
        assertEquals("Tarea Domingo", tareasObtenidas.get(4).getDescripcion());

        // Verificar las fechas relativas
        assertEquals(fechaBaseSemana, tareasObtenidas.get(0).getFecha()); // Lunes
        assertEquals(fechaBaseSemana.plusDays(1), tareasObtenidas.get(1).getFecha()); // Martes
        assertEquals(fechaBaseSemana.plusDays(2), tareasObtenidas.get(2).getFecha()); // Miércoles
        assertEquals(fechaBaseSemana.plusDays(4), tareasObtenidas.get(3).getFecha()); // Viernes
        assertEquals(fechaBaseSemana.plusDays(6), tareasObtenidas.get(4).getFecha()); // Domingo

        // Verificar que todas son del tipo UNA_VEZ
        tareasObtenidas.forEach(tarea -> 
            assertEquals(TipoRecurrencia.UNA_VEZ, tarea.getTipoRecurrencia())
        );
    }

    @Test
    void testObtenerTareasPorSemana_ExcluyeTareasFueraDeLaSemana() {
        // given - Las tareas ya están creadas en setUp()
        LocalDate fechaEnLaSemana = fechaBaseSemana.plusDays(5); // Sábado de la semana objetivo
        
        // when
        List<TareaEntity> tareasObtenidas = tareaService.obtenerTareasPorSemana(fechaEnLaSemana);

        // then
        assertNotNull(tareasObtenidas);
        assertEquals(5, tareasObtenidas.size(), "Debe devolver solo las tareas de la semana objetivo");

        // Verificar que no incluye tareas fuera de la semana
        List<String> descripciones = tareasObtenidas.stream()
            .map(TareaEntity::getDescripcion)
            .toList();
        
        // Verificar que contiene las tareas de la semana
        assertTrue(descripciones.contains("Tarea Lunes"));
        assertTrue(descripciones.contains("Tarea Martes"));
        assertTrue(descripciones.contains("Tarea Miércoles"));
        assertTrue(descripciones.contains("Tarea Viernes"));
        assertTrue(descripciones.contains("Tarea Domingo"));
        
        // Verificar que NO contiene tareas fuera de la semana
        assertFalse(descripciones.contains("Tarea Domingo Anterior"));
        assertFalse(descripciones.contains("Tarea Lunes Posterior"));
        assertFalse(descripciones.contains("Tarea Miércoles Posterior"));
        assertFalse(descripciones.contains("Tarea Semana Anterior"));
        assertFalse(descripciones.contains("Tarea Semana Posterior"));

        // Verificar que están ordenadas por fecha
        for (int i = 0; i < tareasObtenidas.size() - 1; i++) {
            assertTrue(tareasObtenidas.get(i).getFecha().isBefore(tareasObtenidas.get(i + 1).getFecha()) ||
                       tareasObtenidas.get(i).getFecha().isEqual(tareasObtenidas.get(i + 1).getFecha()));
        }
    }

    @Test
    void testObtenerTareasPorSemana_SemanaConMenosTareas() {
        // given - Las tareas ya están creadas en setUp()
        // Usar la semana donde tenemos "Tarea Semana Posterior" (2 semanas después)
        LocalDate fechaEnOtraSemana = fechaBaseSemana.plusWeeks(2); // Lunes de la semana lejana
        
        // when
        List<TareaEntity> tareasObtenidas = tareaService.obtenerTareasPorSemana(fechaEnOtraSemana);

        // then
        assertNotNull(tareasObtenidas);
        assertEquals(1, tareasObtenidas.size(), "Debe devolver solo 1 tarea de la semana lejana");
        assertEquals("Tarea Semana Posterior", tareasObtenidas.getFirst().getDescripcion());
        assertEquals(fechaBaseSemana.plusWeeks(2), tareasObtenidas.getFirst().getFecha());
    }

    @Test
    void testObtenerTareasPorSemana_VerificarTotalTareasCreadas() {
        // given - Las tareas ya están creadas en setUp()
        
        // when - Obtener todas las tareas creadas
        List<TareaEntity> todasLasTareas = tareaRepository.findAll();

        // then
        assertEquals(10, todasLasTareas.size(), "Debe haber exactamente 10 tareas creadas en total");
        
        // Verificar que todas son del tipo UNA_VEZ
        todasLasTareas.forEach(tarea -> 
            assertEquals(TipoRecurrencia.UNA_VEZ, tarea.getTipoRecurrencia())
        );
        
        // Verificar que todas tienen el mismo puesto y turno
        todasLasTareas.forEach(tarea -> {
            assertEquals(puesto.getId(), tarea.getPuesto().getId());
            assertEquals(turno.getId(), tarea.getTurno().getId());
        });
    }

    @Test
    void testBuscarPorRangoFechas_FechaInicioSolamente() {
        // given - Usar una fecha específica de las tareas creadas
        LocalDate fechaBuscada = fechaBaseSemana.plusDays(2); // Miércoles
        FiltroTareaRequest filtro = new FiltroTareaRequest(
            null, null, null, null, null, null, 
            fechaBuscada, null // solo fecha inicio
        );

        // when
        List<TareaEntity> tareasEncontradas = tareaService.buscar(filtro);

        // then
        assertNotNull(tareasEncontradas);
        assertEquals(1, tareasEncontradas.size(), "Debe encontrar exactamente 1 tarea para esa fecha específica");
        assertEquals("Tarea Miércoles", tareasEncontradas.getFirst().getDescripcion());
        assertEquals(fechaBuscada, tareasEncontradas.getFirst().getFecha());
    }

    @Test
    void testBuscarPorRangoFechas_RangoCompleto() {
        // given - Buscar en un rango que incluya varias tareas
        LocalDate fechaInicio = fechaBaseSemana; // Lunes
        LocalDate fechaFin = fechaBaseSemana.plusDays(4); // Viernes
        FiltroTareaRequest filtro = new FiltroTareaRequest(
            null, null, null, null, null, null,
            fechaInicio, fechaFin
        );

        // when
        List<TareaEntity> tareasEncontradas = tareaService.buscar(filtro);

        // then
        assertNotNull(tareasEncontradas);
        assertEquals(4, tareasEncontradas.size(), "Debe encontrar 4 tareas en el rango Lunes-Viernes");
        
        // Verificar que contiene las tareas esperadas
        List<String> descripciones = tareasEncontradas.stream()
            .map(TareaEntity::getDescripcion)
            .toList();
        
        assertTrue(descripciones.contains("Tarea Lunes"));
        assertTrue(descripciones.contains("Tarea Martes"));
        assertTrue(descripciones.contains("Tarea Miércoles"));
        assertTrue(descripciones.contains("Tarea Viernes"));
        assertFalse(descripciones.contains("Tarea Domingo")); // No debe incluir domingo que está fuera del rango
    }

    @Test
    void testBuscarPorRangoFechas_RangoSinResultados() {
        // given - Buscar en un rango donde no hay tareas
        LocalDate fechaInicio = fechaBaseSemana.plusDays(3); // Jueves
        LocalDate fechaFin = fechaBaseSemana.plusDays(3); // Jueves (mismo día)
        FiltroTareaRequest filtro = new FiltroTareaRequest(
            null, null, null, null, null, null,
            fechaInicio, fechaFin
        );

        // when
        List<TareaEntity> tareasEncontradas = tareaService.buscar(filtro);

        // then
        assertNotNull(tareasEncontradas);
        assertTrue(tareasEncontradas.isEmpty(), "No debe encontrar tareas para el jueves (no hay tarea ese día)");
    }

    @Test
    void testBuscarPorRangoFechas_ErrorFechaFinMenorAInicio() {
        // given - Fechas en orden incorrecto
        LocalDate fechaInicio = fechaBaseSemana.plusDays(4); // Viernes
        LocalDate fechaFin = fechaBaseSemana.plusDays(1); // Martes (anterior al viernes)
        FiltroTareaRequest filtro = new FiltroTareaRequest(
            null, null, null, null, null, null,
            fechaInicio, fechaFin
        );

        // when & then
        AppValidationException exception = assertThrows(AppValidationException.class, () -> {
            tareaService.buscar(filtro);
        });

        // Verificar el mensaje de error en el mapa de errores
        assertTrue(exception.getErrors().containsKey("fechaFin"));
        assertTrue(exception.getErrors().get("fechaFin").contains("La fecha fin debe ser posterior o igual a la fecha inicio"));
    }

    @Test
    void testBuscarPorRangoFechas_RangoAmplio() {
        // given - Buscar en un rango amplio que incluya tareas de múltiples semanas
        LocalDate fechaInicio = fechaBaseSemana.minusWeeks(1); // Semana anterior
        LocalDate fechaFin = fechaBaseSemana.plusWeeks(1).plusDays(6); // Domingo de la semana posterior
        FiltroTareaRequest filtro = new FiltroTareaRequest(
            null, null, null, null, null, null,
            fechaInicio, fechaFin
        );

        // when
        List<TareaEntity> tareasEncontradas = tareaService.buscar(filtro);

        // then
        assertNotNull(tareasEncontradas);
        assertEquals(9, tareasEncontradas.size(), "Debe encontrar 9 tareas en el rango amplio");
        
        // Verificar que incluye tareas de diferentes semanas
        List<String> descripciones = tareasEncontradas.stream()
            .map(TareaEntity::getDescripcion)
            .toList();
        
        assertTrue(descripciones.contains("Tarea Semana Anterior"));
        assertTrue(descripciones.contains("Tarea Domingo Anterior"));
        assertTrue(descripciones.contains("Tarea Lunes")); // Semana principal
        assertTrue(descripciones.contains("Tarea Lunes Posterior"));
        assertTrue(descripciones.contains("Tarea Miércoles Posterior"));
        assertFalse(descripciones.contains("Tarea Semana Posterior")); // Esta está 2 semanas después, fuera del rango
    }
}