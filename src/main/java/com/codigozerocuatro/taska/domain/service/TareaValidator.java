package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.exception.AppValidationException;
import com.codigozerocuatro.taska.domain.model.DiaSemana;
import com.codigozerocuatro.taska.domain.model.ErrorCode;
import com.codigozerocuatro.taska.domain.model.TareaValida;
import com.codigozerocuatro.taska.domain.model.TipoRecurrencia;
import com.codigozerocuatro.taska.infra.dto.CrearTareaRequest;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Component
public class TareaValidator {

    private static final String ERROR_TIPO_RECURRENCIA_INVALIDO = "Tipo de recurrencia inválido: %s. Valores válidos: %s";
    private static final String ERROR_FECHA_REQUERIDA = "El campo 'fecha' es obligatorio para recurrencia UNA_VEZ";
    private static final String ERROR_FECHA_PASADA = "La fecha debe ser posterior o igual a la fecha actual";
    private static final String ERROR_FECHA_MAXIMA_SUPERA_UN_ANIO = "La fecha máxima no puede superar 1 año desde la fecha actual";
    private static final String ERROR_FECHA_MAXIMA_MENOR_A_INICIO = "La fecha máxima debe ser posterior a la fecha de inicio";
    private static final String ERROR_DIA_SEMANA_INVALIDO = "Día de la semana inválido: %s. Valores válidos: %s";
    private static final String ERROR_RANGO_FECHAS_INVALIDO = "La fecha fin debe ser posterior o igual a la fecha inicio";

    // Valores por defecto para número de repeticiones
    private static final int REPETICIONES_DIARIA_DEFAULT = 90;    // 3 meses
    private static final int REPETICIONES_SEMANAL_DEFAULT = 52;   // 1 año
    private static final int REPETICIONES_QUINCENAL_DEFAULT = 26; // 1 año
    private static final int REPETICIONES_MENSUAL_DEFAULT = 12;   // 1 año
    private static final int REPETICIONES_MAX = 365;             // Máximo 1 año


    private static final EnumSet<TipoRecurrencia> RECURRENCIAS_CON_DIA_SEMANA =
            EnumSet.of(TipoRecurrencia.SEMANAL, TipoRecurrencia.QUINCENAL);

    private static final EnumSet<TipoRecurrencia> RECURRENCIAS_CON_DIA_MES =
            EnumSet.of(TipoRecurrencia.MENSUAL);

    public TareaValida validarTareaRequest(CrearTareaRequest request) {

        TipoRecurrencia tipoRecurrencia = getTipoRecurrencia(request.tipoRecurrencia());

        DiaSemana diaSemana = validarDiaSemana(
                request.diaSemana(),
                RECURRENCIAS_CON_DIA_SEMANA.contains(tipoRecurrencia)
        );

        Integer diaMes = validarDiaMes(
                request.diaMes(),
                RECURRENCIAS_CON_DIA_MES.contains(tipoRecurrencia),
                (d) -> d >= 1 && d <= 31
        );

        LocalDate fecha = validarFecha(request.fecha(), TipoRecurrencia.UNA_VEZ.equals(tipoRecurrencia));

        LocalDate fechaMaxima = validarFechaMaxima(request.fechaMaxima(), fecha, tipoRecurrencia);

        Integer numeroRepeticiones = validarNumeroRepeticiones(request.numeroRepeticiones(), tipoRecurrencia);

        return new TareaValida(
                request.descripcion(),
                request.puestoId(),
                request.turnoId(),
                tipoRecurrencia,
                diaSemana,
                diaMes,
                fecha,
                numeroRepeticiones,
                fechaMaxima
        );
    }

    /**
     * Convierte la representación de cadena proporcionada en su correspondiente constante del enum {@link TipoRecurrencia}.
     * Si el valor proporcionado no es un {@link TipoRecurrencia} válido, se lanza una {@link ValidationException}
     * con un mensaje de error apropiado.
     *
     * @param tipoRecurrenciaStr la representación en cadena del {@link TipoRecurrencia} a analizar
     * @return la constante del enum {@link TipoRecurrencia} correspondiente a la cadena de entrada
     * @throws ValidationException si la cadena de entrada no puede convertirse en un {@link TipoRecurrencia} válido
     */
    public TipoRecurrencia getTipoRecurrencia(String tipoRecurrenciaStr) {
        return parseEnum(
                tipoRecurrenciaStr,
                TipoRecurrencia.class,
                () -> String.format(ERROR_TIPO_RECURRENCIA_INVALIDO,
                        tipoRecurrenciaStr,
                        String.join(", ", getEnumNames(TipoRecurrencia.class))),
                "tipoRecurrencia"
        );
    }

    /**
     * Convierte un valor de cadena en su correspondiente constante del enum {@link DiaSemana}.
     * Si el valor proporcionado no coincide con ningún {@link DiaSemana} válido,
     * se lanza una {@link ValidationException} con un mensaje de error apropiado.
     *
     * @param valor la representación en cadena del {@link DiaSemana} a analizar
     * @return la constante del enum {@link DiaSemana} correspondiente a la cadena de entrada
     * @throws ValidationException si la cadena de entrada no puede convertirse en un {@link DiaSemana} válido
     */
    public DiaSemana getDiaSemana(String valor) {
        return parseEnum(
                valor,
                DiaSemana.class,
                () -> String.format(ERROR_DIA_SEMANA_INVALIDO,
                        valor,
                        String.join(", ", getEnumNames(DiaSemana.class))),
                "diaSemana"
        );
    }

    private DiaSemana validarDiaSemana(String valor, boolean esRequerido) {
        if (!esRequerido) {
            return null;
        }

        return getDiaSemana(valor);
    }

    private Integer validarDiaMes(
            Integer valor,
            boolean esRequerido,
            Predicate<Integer> validadorRango
    ) {
        if (!esRequerido) {
            return null;
        }

        if(valor == null) {
            throw new AppValidationException("diaMes", ErrorCode.DIA_MES_REQUIRED);
        }

        if (!validadorRango.test(valor)) {
            throw new AppValidationException("diaMes", ErrorCode.DIA_MES_INVALID_RANGE);
        }

        return valor;
    }

    private LocalDate validarFecha(LocalDate fecha, boolean esRequerido) {
        if (!esRequerido) {
            return null;
        }

        if (fecha == null) {
            throw new AppValidationException("fecha", ERROR_FECHA_REQUERIDA);
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new AppValidationException("fecha", ERROR_FECHA_PASADA);
        }

        return fecha;
    }

    private LocalDate validarFechaMaxima(LocalDate fechaMaxima, LocalDate fechaInicio, TipoRecurrencia tipoRecurrencia) {
        // Para UNA_VEZ, no se aplica fecha máxima
        if (tipoRecurrencia == TipoRecurrencia.UNA_VEZ) {
            return null;
        }

        // Si no se especifica fecha máxima, no hay validación
        if (fechaMaxima == null) {
            return null;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate unAnioDesdeHoy = hoy.plusYears(1);

        // Validar que no supere 1 año desde hoy
        if (fechaMaxima.isAfter(unAnioDesdeHoy)) {
            throw new AppValidationException("fechaMaxima", ERROR_FECHA_MAXIMA_SUPERA_UN_ANIO);
        }

        // Validar que sea posterior a la fecha actual
        if (fechaMaxima.isBefore(hoy)) {
            throw new AppValidationException("fechaMaxima", ERROR_FECHA_PASADA);
        }

        // Si hay fecha de inicio, validar que fechaMaxima sea posterior
        if (fechaInicio != null && fechaMaxima.isBefore(fechaInicio)) {
            throw new AppValidationException("fechaMaxima", ERROR_FECHA_MAXIMA_MENOR_A_INICIO);
        }

        return fechaMaxima;
    }

    private Integer validarNumeroRepeticiones(Integer numeroRepeticiones, TipoRecurrencia tipoRecurrencia) {
        // Para UNA_VEZ, no se aplican repeticiones
        if (tipoRecurrencia == TipoRecurrencia.UNA_VEZ) {
            return 1;
        }

        // Si no se especifica, usar valores por defecto
        if (numeroRepeticiones == null) {
            return switch (tipoRecurrencia) {
                case DIARIA -> REPETICIONES_DIARIA_DEFAULT;
                case SEMANAL -> REPETICIONES_SEMANAL_DEFAULT;
                case QUINCENAL -> REPETICIONES_QUINCENAL_DEFAULT;
                case MENSUAL -> REPETICIONES_MENSUAL_DEFAULT;
                default -> 1;
            };
        }

        // Validar rango
        if (numeroRepeticiones < 1 || numeroRepeticiones > REPETICIONES_MAX) {
            throw new AppValidationException("numeroRepeticiones", ErrorCode.NUMERO_REPETICIONES_INVALID_RANGE);
        }

        return numeroRepeticiones;
    }

    /**
     * Analiza un valor de cadena y lo convierte en la constante del enum correspondiente
     * de la clase de enum especificada. Si el valor no puede analizarse, se lanza una
     * {@link ValidationException} con el mensaje de error proporcionado.
     *
     * @param value la representación en cadena de la constante del enum a analizar
     * @param enumClass la clase del enum en la que se desea convertir
     * @param errorMessage un proveedor del mensaje de error a usar en la excepción si falla el análisis
     * @param <T> el tipo del enum
     * @return la constante del enum analizada del tipo especificado
     * @throws ValidationException si el valor no puede convertirse en una constante válida del enum
     */
    private static <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass, Supplier<String> errorMessage, String fieldName) {
        if (value == null) {
            throw new AppValidationException(Map.of(fieldName, errorMessage.get()));
        }

        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new AppValidationException(Map.of(fieldName, errorMessage.get()));
        }
    }

    private static <T extends Enum<T>> String[] getEnumNames(Class<T> enumClass) {
        return java.util.Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    /**
     * Valida el rango de fechas para búsqueda
     * @param fechaInicio fecha de inicio (obligatoria si se usa rango)
     * @param fechaFin fecha de fin (opcional)
     * @throws AppValidationException si las fechas no son válidas
     */
    public void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new AppValidationException("fechaFin", ERROR_RANGO_FECHAS_INVALIDO);
        }
    }
}