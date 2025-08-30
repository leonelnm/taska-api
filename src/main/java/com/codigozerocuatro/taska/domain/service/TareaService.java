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
}
