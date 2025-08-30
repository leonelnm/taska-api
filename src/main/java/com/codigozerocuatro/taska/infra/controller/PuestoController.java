package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.service.PuestoService;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/puestos")
public class PuestoController {

    private final PuestoService puestoService;

    @GetMapping
    public ResponseEntity<List<PuestoEntity>> getAllPuestos(){
        return ResponseEntity.ok(puestoService.findAll());
    }

}
