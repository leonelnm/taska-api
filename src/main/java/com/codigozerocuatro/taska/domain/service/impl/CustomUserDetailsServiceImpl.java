package com.codigozerocuatro.taska.domain.service.impl;

import com.codigozerocuatro.taska.domain.model.User;
import com.codigozerocuatro.taska.domain.service.UserService;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.obtenerUsuarioPorUsernameActivo(username, true);
        return new User(user);
    }

}
