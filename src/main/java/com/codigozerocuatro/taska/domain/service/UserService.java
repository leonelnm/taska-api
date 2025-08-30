package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.infra.dto.CrearUserRequest;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity crearUsuario(CrearUserRequest request);

    UserEntity obtenerUsuarioPorUsername(String username);

    UserEntity obtenerUsuarioPorUsernameActivo(String username, boolean activo);

    void desactivarUsuario(String username);

    void activarUsuario(String username);

    void adminChangePassword(String username, String password);

    List<UserEntity> buscarTodos();
}
