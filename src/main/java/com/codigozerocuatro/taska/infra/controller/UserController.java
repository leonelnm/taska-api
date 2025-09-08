package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.service.UserService;
import com.codigozerocuatro.taska.infra.dto.AdminChangePasswordRequest;
import com.codigozerocuatro.taska.infra.dto.CrearUserRequest;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserEntity> create(@Valid @RequestBody CrearUserRequest request){
        UserEntity user = userService.crearUsuario(request);
        user.setPassword(":)");
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getUsers(){
        return ResponseEntity.ok(userService.buscarTodos());
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody AdminChangePasswordRequest request){
        userService.adminChangePassword(request.username(), request.password());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{username}/activar")
    public ResponseEntity<Void> activate(@PathVariable String username){
        userService.activarUsuario(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{username}/desactivar")
    public ResponseEntity<Void> deactivate(@PathVariable String username){
        userService.desactivarUsuario(username);
        return ResponseEntity.noContent().build();
    }

}
