package com.codigozerocuatro.taska.infra.config;

import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import com.codigozerocuatro.taska.domain.model.TurnoEnum;
import com.codigozerocuatro.taska.infra.persistence.model.PuestoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.TurnoEntity;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import com.codigozerocuatro.taska.infra.persistence.repository.PuestoJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.repository.TurnoJpaRepository;
import com.codigozerocuatro.taska.infra.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader implements CommandLineRunner {

    private final PuestoJpaRepository puestoRepository;
    private final TurnoJpaRepository turnoRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        loadUserAdmin();
        loadPuestos();
        loadTurnos();
    }


    private void loadPuestos() {
        if (puestoRepository.count() == 0) {
            List<PuestoEntity> puestos = List.of(
                    new PuestoEntity(PuestoEnum.ADMINISTRADOR),
                    new PuestoEntity(PuestoEnum.COCINERO),
                    new PuestoEntity(PuestoEnum.CAMARERO),
                    new PuestoEntity(PuestoEnum.PINCHE)
            );

            puestoRepository.saveAll(puestos);
            log.info("Puestos iniciales cargados");
        }
    }

    private void loadTurnos() {
        if (turnoRepository.count() == 0) {
            List<TurnoEntity> turnos = List.of(
                    new TurnoEntity(TurnoEnum.MANANA),
                    new TurnoEntity(TurnoEnum.MEDIO),
                    new TurnoEntity(TurnoEnum.TARDE)
            );

            turnoRepository.saveAll(turnos);
            log.info("Turnos iniciales cargados");
        }
    }

    private void loadUserAdmin() {
        if(userJpaRepository.findByUsername("admin").isPresent()){
            return;
        }

        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode(appProperties.user().defaultPassword()));
        user.setNombre("Admin");
        user.setRoles(Set.of(PuestoEnum.ADMINISTRADOR));

        userJpaRepository.save(user);
        log.info("Usuario admin cargado");
    }

}
