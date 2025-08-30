package com.codigozerocuatro.taska.infra.persistence.repository;

import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, Long> {
}
