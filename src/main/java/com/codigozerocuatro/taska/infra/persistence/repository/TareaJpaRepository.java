package com.codigozerocuatro.taska.infra.persistence.repository;

import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TareaJpaRepository extends JpaRepository<TareaEntity, Long>, JpaSpecificationExecutor<TareaEntity> {

}
