package com.codigozerocuatro.taska.domain.service.impl;

import com.codigozerocuatro.taska.domain.model.CacheKey;
import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.domain.service.PuestoService;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.PuestoJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PuestoServiceImpl implements PuestoService {

    private final PuestoJpaRepository repository;

    @Override
    public PuestoEntity obtenerPuestoPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Puesto " + id + " no encontrado"));
    }

    @Override
    public PuestoEntity obtenerPuestoPorNombre(PuestoEnum puestoEnum) {
        return repository.findByPuesto(puestoEnum)
                .orElseThrow(() -> new EntityNotFoundException("Puesto " + puestoEnum.name() + " no encontrado"));
    }

    @Cacheable(CacheKey.PUESTOS)
    @Override
    public List<PuestoEntity> findAll() {
        return repository.findAll();
    }
}
