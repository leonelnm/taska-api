package com.codigozerocuatro.taska.infra.persistence.repository;

import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    @Cacheable(value = "USER", key = "#username", cacheManager = "cacheManagerUser")
    Optional<UserEntity> findByUsername(String username);

    @Cacheable(value = "USER", key = "#username + '_' + #activo", cacheManager = "cacheManagerUser")
    Optional<UserEntity> findByUsernameAndActivo(String username, boolean activo);

}
