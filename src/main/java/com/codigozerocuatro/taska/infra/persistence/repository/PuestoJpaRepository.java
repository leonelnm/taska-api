package com.codigozerocuatro.taska.infra.persistence.repository;

import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PuestoJpaRepository extends JpaRepository<PuestoEntity, Long> {

    Optional<PuestoEntity> findByPuesto(PuestoEnum puesto);

}
