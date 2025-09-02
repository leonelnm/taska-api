package com.codigozerocuatro.taska.infra.config;

import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.domain.model.RolEnum;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader implements CommandLineRunner {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Transactional
    @Override
    public void run(String... args){
        loadUserAdmin();
    }

    private void loadUserAdmin() {
        if(userJpaRepository.findByUsername("admin").isPresent()){
            return;
        }

        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode(appProperties.user().defaultPassword()));
        user.setNombre("Admin");
        user.setRol(RolEnum.ADMIN);
        user.setPuesto(PuestoEnum.ENCARGADO);

        userJpaRepository.save(user);
        log.info("Usuario admin cargado");
    }

}
