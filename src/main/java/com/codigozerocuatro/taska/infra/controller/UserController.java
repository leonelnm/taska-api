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
@PreAuthorize("hasRole('ADMINISTRADOR')")
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
    public void changePassword(@RequestBody AdminChangePasswordRequest request){
        userService.adminChangePassword(request.username(), request.password());
    }

    @PutMapping("/{username}/activar")
    public void activate(@PathVariable String username){
        userService.activarUsuario(username);
    }

    @PutMapping("/{username}/desactivar")
    public void deactivate(@PathVariable String username){
        userService.desactivarUsuario(username);
    }

}
