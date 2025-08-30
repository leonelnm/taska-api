package com.codigozerocuatro.taska.infra.persistence.repository;

import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByUsernameAndActivo(String username, boolean activo);

}
