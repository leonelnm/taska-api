package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import com.codigozerocuatro.taska.infra.dto.FiltroTareaRequest;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;

import java.time.LocalDate;
import java.util.List;

public interface TareaService {

    TareaEntity crear(CrearTareaRequest request);

    List<TareaEntity> todas();

    List<TareaEntity> buscar(FiltroTareaRequest filtro);

    TareaEntity completar(Long id);

    List<TareaEntity> crearTodas(List<CrearTareaRequest> requests);

    /**
     * Obtiene todas las tareas de una serie recurrente (padre e hijas)
     */
    List<TareaEntity> obtenerSerieRecurrente(Long idTareaPadre);

    /**
     * Obtiene todas las tareas de una semana específica ordenadas por fecha
     * @param fecha cualquier fecha dentro de la semana deseada
     * @return lista de tareas de esa semana ordenadas por fecha de menor a mayor
     */
    List<TareaEntity> obtenerTareasPorSemana(LocalDate fecha);

    /**
     * Elimina solo una tarea específica. Si la tarea es padre, actualiza las hijas
     * y convierte la siguiente tarea por fecha en la nueva tarea padre.
     */
    void eliminarSoloTarea(Long id);

    /**
     * Elimina una tarea específica y todas las tareas posteriores en la serie recurrente
     */
    void eliminarTareaYPosteriores(Long id);
}
