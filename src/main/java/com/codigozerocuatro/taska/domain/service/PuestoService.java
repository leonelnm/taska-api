package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;

import java.util.List;

public interface PuestoService {

    PuestoEntity obtenerPuestoPorId(Long id);

    PuestoEntity obtenerPuestoPorNombre(PuestoEnum puestoEnum);

    List<PuestoEntity> findAll();
}
