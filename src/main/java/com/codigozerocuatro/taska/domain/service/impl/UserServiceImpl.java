package com.codigozerocuatro.taska.domain.service.impl;

import com.codigozerocuatro.taska.domain.exception.AppValidationException;
import com.codigozerocuatro.taska.domain.model.ErrorCode;
import com.codigozerocuatro.taska.domain.model.RolEnum;
import com.codigozerocuatro.taska.domain.service.PuestoService;
import com.codigozerocuatro.taska.domain.service.UserService;
import com.codigozerocuatro.taska.infra.dto.CrearUserRequest;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final PuestoService puestoService;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity crearUsuario(CrearUserRequest request) {

        PuestoEntity puesto = puestoService.obtenerPuestoPorId(request.puestoId());

        Optional<UserEntity> userFound = userJpaRepository.findByUsername(request.username());
        if (userFound.isPresent()) {
            throw new AppValidationException(Map.of("username", ErrorCode.USERNAME_ALREADY_EXISTS));
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.username());
        newUser.setNombre(request.nombre());
        newUser.setPuesto(puesto.getPuesto());
        newUser.setRol(RolEnum.USER);
        newUser.setPassword(passwordEncoder.encode(request.password()));
        return userJpaRepository.save(newUser);
    }

    @Override
    public UserEntity obtenerUsuarioPorUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User no encontrado con username: " + username));
    }

    @Override
    public UserEntity obtenerUsuarioPorUsernameActivo(String username, boolean activo) {
        return userJpaRepository.findByUsernameAndActivo(username, activo)
                .orElseThrow(() -> new UsernameNotFoundException("User no encontrado con username: " + username));
    }

    @Override
    public void desactivarUsuario(String username) {
        UserEntity user = obtenerUsuarioPorUsernameActivo(username, true);
        user.setActivo(false);
        userJpaRepository.save(user);
    }

    @Override
    public void activarUsuario(String username) {
        UserEntity user = obtenerUsuarioPorUsernameActivo(username, false);
        user.setActivo(true);
        userJpaRepository.save(user);
    }

    @Override
    public void adminChangePassword(String username, String password) {
        UserEntity user = obtenerUsuarioPorUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userJpaRepository.save(user);
        log.info("Password cambiado por el administrador");
    }

    @Override
    public List<UserEntity> buscarTodos() {
        List<UserEntity> users = userJpaRepository.findAll();
        users.forEach(user -> user.setPassword(":)"));
        return users;
    }

    @Override
    public void changePassword(UserEntity user, String currentPassword, String newPassword) {
        if (newPassword.equals(currentPassword)) {
            throw new AppValidationException(Map.of("password", ErrorCode.PASSWORD_SAME_BEFORE));
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AppValidationException(Map.of("password", ErrorCode.PASSWORD_INCORRECT));
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userJpaRepository.save(user);
    }
}
