package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import com.codigozerocuatro.taska.infra.dto.FiltroTareaRequest;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;

import java.util.List;

public interface TareaService {

    TareaEntity crear(CrearTareaRequest request);

    List<TareaEntity> todas();

    List<TareaEntity> buscar(FiltroTareaRequest filtro);

    TareaEntity completar(Long id);

    List<TareaEntity> crearTodas(List<CrearTareaRequest> requests);

    /**
     * Elimina una tarea y todas sus tareas hijas (si las tiene)
     */
    void eliminarTareaYSusHijas(Long id);

    /**
     * Elimina una tarea específica y todas las tareas posteriores en la serie recurrente
     */
    void eliminarTareaYPosteriores(Long id);

    /**
     * Actualiza la descripción de una tarea y todas las tareas posteriores en la serie recurrente
     */
    void actualizarTareaYPosteriores(Long id, String nuevaDescripcion);

    /**
     * Obtiene todas las tareas de una serie recurrente (padre e hijas)
     */
    List<TareaEntity> obtenerSerieRecurrente(Long idTareaPadre);
}
