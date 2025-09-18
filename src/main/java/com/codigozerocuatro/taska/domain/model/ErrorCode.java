package com.codigozerocuatro.taska.domain.model;

public final class ErrorCode {

    private ErrorCode() {
        throw new RuntimeException("utility class");
    }

    // Genérico
    public static final String VALIDATION_FAILED = "error.validation.failed";
    public static final String VALIDATION_UNKNOWN = "error.validation.unknown";
    public static final String ENTITY_NOT_FOUND = "error.entity.not_found";
    public static final String RESOURCE_NOT_FOUND = "error.resource.not_found";
    public static final String METHOD_NOT_ALLOWED = "error.http.method_not_allowed";
    public static final String INTERNAL_ERROR = "error.internal";

    // Auth
    public static final String BAD_CREDENTIALS = "error.auth.bad_credentials";
    public static final String ACCESS_DENIED = "error.auth.access_denied";

    // Usuario
    public static final String USERNAME_REQUIRED = "error.username.required";
    public static final String USERNAME_MIN_LENGTH = "error.username.min_length";
    public static final String USERNAME_MAX_LENGTH = "error.username.max_length";
    public static final String USERNAME_INVALID_CHARS = "error.username.invalid_chars";
    public static final String USERNAME_ALREADY_EXISTS = "error.username.already_exists";

    public static final String PASSWORD_REQUIRED = "error.password.required";
    public static final String PASSWORD_MIN_LENGTH = "error.password.min_length";
    public static final String PASSWORD_MAX_LENGTH = "error.username.max_length";
    public static final String PASSWORD_NO_SPACES = "error.password.no_spaces";
    public static final String PASSWORD_COMPLEXITY = "error.password.complexity";
    public static final String PASSWORD_SAME_CURRENT = "error.password.same_as_current";
    public static final String PASSWORD_INCORRECT = "error.password.incorrect";

    public static final String PUESTO_REQUIRED = "error.puesto.required";

    // Tarea
    public static final String DESCRIPCION_REQUIRED = "error.descripcion.required";
    public static final String TURNO_REQUIRED = "error.turno.required";
    public static final String TIPO_RECURRENCIA_REQUIRED = "error.tipoRecurrencia.required";
    public static final String DIA_MES_INVALID_RANGE = "error.diames.invalid.range";
}

