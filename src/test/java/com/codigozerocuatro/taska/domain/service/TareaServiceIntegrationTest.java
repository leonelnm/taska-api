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
import java.util.Comparator;
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

    @Test
    void testEliminarSoloTarea_EliminaSoloUnaTarea() {
        // given - Limpiar las tareas existentes y crear una serie recurrente SEMANAL con 4 tareas
        tareaRepository.deleteAll();
        
        LocalDate fechaInicio = LocalDate.now().plusDays(7); // Una semana desde hoy
        CrearTareaRequest request = new CrearTareaRequest(
            "Tarea Semanal Recurrente", 
            puesto.getId(), 
            turno.getId(), 
            TipoRecurrencia.SEMANAL.name(),
            "LUNES", // Día de la semana
            null,
            fechaInicio,
            4, // 4 repeticiones
            null
        );

        // Crear la serie de tareas
        TareaEntity tareaPadre = tareaService.crear(request);
        
        // Obtener todas las tareas creadas para verificar que son 4
        List<TareaEntity> todasLasTareas = tareaRepository.findAll();
        assertEquals(4, todasLasTareas.size(), "Debe haber 4 tareas creadas");
        
        // Obtener la serie completa
        List<TareaEntity> serieCompleta = tareaService.obtenerSerieRecurrente(tareaPadre.getId());
        assertEquals(4, serieCompleta.size(), "La serie debe tener 4 tareas");
        
        // Ordenar por fecha para tener un orden predecible
        serieCompleta.sort(Comparator.comparing(TareaEntity::getFecha));
        
        // when - Eliminar la segunda tarea de la serie (no la primera que es padre)
        Long idSegundaTarea = serieCompleta.get(1).getId();
        tareaService.eliminarSoloTarea(idSegundaTarea);

        // then - Verificar que solo se eliminó una tarea
        List<TareaEntity> tareasRestantes = tareaRepository.findAll();
        assertEquals(3, tareasRestantes.size(), "Debe quedar exactamente 3 tareas después de eliminar una");
        
        // Verificar que la tarea eliminada ya no existe
        assertFalse(tareaRepository.existsById(idSegundaTarea), "La segunda tarea debe haber sido eliminada");

        // Verificar que las demás tareas siguen existiendo
        assertTrue(tareaRepository.existsById(tareasRestantes.get(0).getId()), "La primera tarea debe seguir existiendo");
        assertTrue(tareaRepository.existsById(tareasRestantes.get(1).getId()), "La tercera tarea debe seguir existiendo");
        assertTrue(tareaRepository.existsById(tareasRestantes.get(2).getId()), "La cuarta tarea debe seguir existiendo");

        // Verificar que la tarea padre sigue siendo padre (sin idTareaPadre)
        TareaEntity tareaPadreActual = tareaRepository.findById(tareaPadre.getId()).orElseThrow();
        assertNull(tareaPadreActual.getIdTareaPadre(), "La tarea padre debe seguir siendo padre");
    }

    @Test
    void testEliminarTareaYPosteriores_EliminaTareaYTodasLasPosteriores() {
        // given - Limpiar las tareas existentes y crear una serie recurrente QUINCENAL con 4 tareas
        tareaRepository.deleteAll();
        
        LocalDate fechaInicio = LocalDate.now().plusDays(14); // Dos semanas desde hoy
        CrearTareaRequest request = new CrearTareaRequest(
            "Tarea Quincenal Recurrente", 
            puesto.getId(), 
            turno.getId(), 
            TipoRecurrencia.QUINCENAL.name(),
            "MIERCOLES", // Día de la semana
            null,
            fechaInicio,
            4, // 4 repeticiones
            null
        );

        // Crear la serie de tareas
        TareaEntity tareaPadre = tareaService.crear(request);
        
        // Obtener todas las tareas creadas para verificar que son 4
        List<TareaEntity> todasLasTareas = tareaService.todas();
        assertEquals(4, todasLasTareas.size(), "Debe haber 4 tareas creadas");
        
        // Obtener la serie completa y ordenar por fecha
        List<TareaEntity> serieCompleta = tareaService.obtenerSerieRecurrente(tareaPadre.getId());
        assertEquals(4, serieCompleta.size(), "La serie debe tener 4 tareas");
        serieCompleta.sort(Comparator.comparing(TareaEntity::getFecha));
        
        // when - Eliminar la segunda tarea y todas las posteriores
        Long idSegundaTarea = serieCompleta.get(1).getId();
        tareaService.eliminarTareaYPosteriores(idSegundaTarea);

        // then - Verificar que se eliminaron la segunda, tercera y cuarta tarea
        List<TareaEntity> tareasRestantes = tareaRepository.findAll();
        assertEquals(1, tareasRestantes.size(), "Debe quedar solo 1 tarea después de eliminar las posteriores");
        
        // Verificar que solo la primera tarea existe
        assertTrue(tareaRepository.existsById(serieCompleta.get(0).getId()), "La primera tarea debe seguir existiendo");
        
        // Verificar que las tareas posteriores fueron eliminadas
        assertFalse(tareaRepository.existsById(serieCompleta.get(1).getId()), "La segunda tarea debe haber sido eliminada");
        assertFalse(tareaRepository.existsById(serieCompleta.get(2).getId()), "La tercera tarea debe haber sido eliminada");
        assertFalse(tareaRepository.existsById(serieCompleta.get(3).getId()), "La cuarta tarea debe haber sido eliminada");
        
        // Verificar que la tarea restante sigue siendo válida
        TareaEntity tareaRestante = tareasRestantes.getFirst();
        assertEquals("Tarea Quincenal Recurrente", tareaRestante.getDescripcion());
        assertEquals(TipoRecurrencia.QUINCENAL, tareaRestante.getTipoRecurrencia());
        assertNull(tareaRestante.getIdTareaPadre(), "La tarea restante debe ser padre");
    }

    @Test
    void testEliminarSoloTarea_EliminaTareaPadreYReorganizaHijas() {
        // given - Limpiar las tareas existentes y crear una serie recurrente MENSUAL con 4 tareas
        tareaRepository.deleteAll();
        
        LocalDate fechaInicio = LocalDate.now().plusDays(30); // Un mes desde hoy
        CrearTareaRequest request = new CrearTareaRequest(
            "Tarea Mensual Recurrente", 
            puesto.getId(), 
            turno.getId(), 
            TipoRecurrencia.MENSUAL.name(),
            null, // Sin día de semana para mensual
            15,   // Día 15 del mes
            fechaInicio,
            4,    // 4 repeticiones
            null
        );

        // Crear la serie de tareas
        TareaEntity tareaPadre = tareaService.crear(request);
        
        // Obtener todas las tareas creadas para verificar que son 4
        List<TareaEntity> todasLasTareas = tareaService.todas();
        assertEquals(4, todasLasTareas.size(), "Debe haber 4 tareas creadas");
        
        // Obtener la serie completa y ordenar por fecha
        List<TareaEntity> serieCompleta = tareaService.obtenerSerieRecurrente(tareaPadre.getId());
        serieCompleta.sort(Comparator.comparing(TareaEntity::getFecha));
        assertEquals(4, serieCompleta.size(), "La serie debe tener 4 tareas");
        
        // Verificar que la primera tarea es la padre (sin idTareaPadre)
        TareaEntity tareaPadreOriginal = serieCompleta.getFirst();
        assertNull(tareaPadreOriginal.getIdTareaPadre(), "La primera tarea debe ser la tarea padre original");
        assertEquals(tareaPadre.getId(), tareaPadreOriginal.getId(), "Debe ser la misma tarea padre creada");
        
        // Verificar que las demás son hijas
        for (int i = 1; i < serieCompleta.size(); i++) {
            assertEquals(tareaPadre.getId(), serieCompleta.get(i).getIdTareaPadre(), 
                "Las tareas hijas deben apuntar a la tarea padre");
        }

        // when - Eliminar la tarea padre (primera tarea)
        tareaService.eliminarSoloTarea(tareaPadre.getId());

        // then - Verificar que solo se eliminó la tarea padre
        List<TareaEntity> tareasRestantes = tareaRepository.findAll();
        assertEquals(3, tareasRestantes.size(), "Debe quedar exactamente 3 tareas después de eliminar el padre");
        
        // Verificar que la tarea padre original fue eliminada
        assertFalse(tareaRepository.existsById(tareaPadre.getId()), "La tarea padre original debe haber sido eliminada");
        
        // Verificar que las otras 3 tareas siguen existiendo
        assertTrue(tareaRepository.existsById(serieCompleta.get(1).getId()), "La segunda tarea debe seguir existiendo");
        assertTrue(tareaRepository.existsById(serieCompleta.get(2).getId()), "La tercera tarea debe seguir existiendo");
        assertTrue(tareaRepository.existsById(serieCompleta.get(3).getId()), "La cuarta tarea debe seguir existiendo");
        
        // Verificar que se reorganizó la estructura padre-hijo
        // La segunda tarea (por fecha) debe convertirse en la nueva tarea padre
        TareaEntity nuevaTareaPadre = tareaRepository.findById(serieCompleta.get(1).getId()).orElseThrow();
        assertNull(nuevaTareaPadre.getIdTareaPadre(), "La segunda tarea debe convertirse en la nueva tarea padre");
        
        // Las tareas restantes deben apuntar a la nueva tarea padre
        TareaEntity terceraTarea = tareaRepository.findById(serieCompleta.get(2).getId()).orElseThrow();
        TareaEntity cuartaTarea = tareaRepository.findById(serieCompleta.get(3).getId()).orElseThrow();
        
        assertEquals(nuevaTareaPadre.getId(), terceraTarea.getIdTareaPadre(), 
            "La tercera tarea debe apuntar a la nueva tarea padre");
        assertEquals(nuevaTareaPadre.getId(), cuartaTarea.getIdTareaPadre(), 
            "La cuarta tarea debe apuntar a la nueva tarea padre");
        
        // Verificar que todas las tareas mantienen sus propiedades originales
        assertEquals("Tarea Mensual Recurrente", nuevaTareaPadre.getDescripcion());
        assertEquals("Tarea Mensual Recurrente", terceraTarea.getDescripcion());
        assertEquals("Tarea Mensual Recurrente", cuartaTarea.getDescripcion());
    }

    @Test
    void testEliminarTareaYPosteriores_EliminaTareaPadreYTodasLasHijas() {
        // given - Limpiar las tareas existentes y crear una serie recurrente DIARIA con 4 tareas
        tareaRepository.deleteAll();
        
        LocalDate fechaInicio = LocalDate.now().plusDays(5); // 5 días desde hoy
        CrearTareaRequest request = new CrearTareaRequest(
            "Tarea Diaria Recurrente", 
            puesto.getId(), 
            turno.getId(), 
            TipoRecurrencia.DIARIA.name(),
            null, // Sin día de semana para diaria
            null, // Sin día de mes para diaria
            fechaInicio,
            4,    // 4 repeticiones
            null
        );

        // Crear la serie de tareas
        TareaEntity tareaPadre = tareaService.crear(request);
        
        // Obtener todas las tareas creadas para verificar que son 4
        List<TareaEntity> todasLasTareas = tareaRepository.findAll();
        assertEquals(4, todasLasTareas.size(), "Debe haber 4 tareas creadas");
        
        // Obtener la serie completa y ordenar por fecha
        List<TareaEntity> serieCompleta = tareaService.obtenerSerieRecurrente(tareaPadre.getId());
        serieCompleta.sort(Comparator.comparing(TareaEntity::getFecha));
        assertEquals(4, serieCompleta.size(), "La serie debe tener 4 tareas");
        
        // Verificar estructura inicial: padre + 3 hijas
        TareaEntity tareaPadreOriginal = serieCompleta.getFirst();
        assertNull(tareaPadreOriginal.getIdTareaPadre(), "La primera tarea debe ser la tarea padre");
        
        for (int i = 1; i < serieCompleta.size(); i++) {
            assertEquals(tareaPadre.getId(), serieCompleta.get(i).getIdTareaPadre(), 
                "Las tareas hijas deben apuntar a la tarea padre");
        }

        // when - Eliminar la tarea padre y todas las posteriores (toda la serie)
        // Este era el caso problemático: integridad referencial padre->hijas
        tareaService.eliminarTareaYPosteriores(tareaPadre.getId());

        // then - Verificar que se eliminaron todas las tareas
        List<TareaEntity> tareasRestantes = tareaRepository.findAll();
        assertEquals(0, tareasRestantes.size(), "No debe quedar ninguna tarea después de eliminar toda la serie");
        
        // Verificar que ninguna de las tareas originales existe
        assertFalse(tareaRepository.existsById(serieCompleta.get(0).getId()), "La primera tarea debe haber sido eliminada");
        assertFalse(tareaRepository.existsById(serieCompleta.get(1).getId()), "La segunda tarea debe haber sido eliminada");
        assertFalse(tareaRepository.existsById(serieCompleta.get(2).getId()), "La tercera tarea debe haber sido eliminada");
        assertFalse(tareaRepository.existsById(serieCompleta.get(3).getId()), "La cuarta tarea debe haber sido eliminada");
    }
}