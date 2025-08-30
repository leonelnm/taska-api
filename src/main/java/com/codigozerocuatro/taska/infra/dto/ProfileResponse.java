package com.codigozerocuatro.taska.infra.dto;

import java.util.List;

public record ProfileResponse(
        String username,
        String name,
        boolean isAdmin,
        List<String> roles
) {
}
