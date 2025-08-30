package com.codigozerocuatro.taska.infra.dto;

public record LoginReponse( String token, String refreshToken, ProfileResponse user) {
}
