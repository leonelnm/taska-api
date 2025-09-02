package com.codigozerocuatro.taska.infra.dto;

public record ProfileResponse(
        String username,
        String name,
        boolean isAdmin,
        String puesto
) {
}
