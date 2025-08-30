package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.model.CacheKey;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.TurnoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoJpaRepository repository;

    @Cacheable(CacheKey.TURNOS)
    @GetMapping
    public ResponseEntity<List<TurnoEntity>> getAll() {
        List<TurnoEntity> turnos = repository.findAll();
        return ResponseEntity.ok(turnos);
    }

}
