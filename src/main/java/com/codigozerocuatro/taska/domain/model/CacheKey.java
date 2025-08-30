package com.codigozerocuatro.taska.domain.model;

public final class CacheKey {

    public static final String TURNOS = "TURNOS";
    public static final String PUESTOS = "PUESTOS";

    private CacheKey(){
        throw new IllegalStateException("Utility class");
    }

}
