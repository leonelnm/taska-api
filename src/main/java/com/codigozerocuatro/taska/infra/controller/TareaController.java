package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.service.TareaService;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import com.codigozerocuatro.taska.infra.dto.FiltroTareaRequest;
import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<TareaEntity> create(@Valid @RequestBody CrearTareaRequest request) {
        TareaEntity tarea = tareaService.crear(request);
        return ResponseEntity.ok(tarea);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/batch")
    public ResponseEntity<List<TareaEntity>> createList(@Valid @RequestBody List<CrearTareaRequest> requests) {
        List<TareaEntity> tareasCreadas = tareaService.crearTodas(requests);
        return ResponseEntity.ok(tareasCreadas);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/all")
    public ResponseEntity<List<TareaEntity>> getAll() {
        List<TareaEntity> tareas = tareaService.todas();
        return ResponseEntity.ok(tareas);
    }

    @GetMapping
    public List<TareaEntity> search(@ModelAttribute FiltroTareaRequest filtro) {
        return tareaService.buscar(filtro);
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<TareaEntity> complete(@PathVariable Long id) {
        TareaEntity tarea = tareaService.completar(id);
        return ResponseEntity.ok(tarea);
    }

}
