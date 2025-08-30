package com.codigozerocuatro.taska.infra.persistence.model;

import com.codigozerocuatro.taska.domain.model.PuestoEnum;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación muchos a muchos con roles (PuestoEnum)
    @ElementCollection(targetClass = PuestoEnum.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "puesto")
    private Set<PuestoEnum> roles = new HashSet<>();

    public UserEntity() {}

    public UserEntity(String username, String password, String nombre) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
    }

    public boolean isAdmin() {
        return roles.stream().anyMatch(role -> role.equals(PuestoEnum.ADMINISTRADOR));
    }

}
