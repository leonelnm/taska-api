package com.codigozerocuatro.taska.infra.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Envolver request y response para poder leer el contenido
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        Instant startTime = Instant.now();
        
        try {
            // Logar información de la petición entrante
            logRequest(wrappedRequest);
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            // Logar información de la respuesta
            Duration duration = Duration.between(startTime, Instant.now());
            logResponse(wrappedRequest, wrappedResponse, duration);
            
            // Importante: copiar el contenido de vuelta al response original
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String queryString = request.getQueryString();
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Request ->: ").append(method).append(" ").append(uri);
        
        if (StringUtils.hasText(queryString)) {
            logMessage.append("?").append(queryString);
        }
        
        // Agregar tamaño de la petición
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            logMessage.append(" | Size: ").append(contentLength).append(" bytes");
        } else if (hasBody(request)) {
            // Si no hay Content-Length pero tiene body, calcular el tamaño del contenido leído
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                logMessage.append(" | Size: ").append(content.length).append(" bytes");
            }
        }

        // Logar headers importantes (sin información sensible)
        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .filter(headerName -> !isSensitiveHeader(headerName))
                .map(headerName -> headerName + "=" + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
        
        if (StringUtils.hasText(headers)) {
            logMessage.append(" | Headers: [").append(headers).append("]");
        }
        
        // Logar body para POST/PUT (con límite de caracteres)
        if (hasBody(request)) {
            String body = getRequestBody(request);
            if (StringUtils.hasText(body)) {
                logMessage.append(" | Body: ").append(truncateIfNeeded(body));
            }
        }
        
        log.info(logMessage.toString());
    }

    private void logResponse(ContentCachingRequestWrapper request, 
                           ContentCachingResponseWrapper response, 
                           Duration duration) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Response <-: ").append(method).append(" ").append(uri);
        logMessage.append(" | Status: ").append(status);
        logMessage.append(" | Duration: ").append(duration.toMillis()).append("ms");
        
        // No logar response body para rutas de autenticación
        boolean isAuthRoute = uri.startsWith("/api/auth/");

        // Logar response body para errores o si es texto/json (excepto rutas de auth)
        if (!isAuthRoute && (status >= 400 || isLoggableResponseType(response))) {
            String responseBody = getResponseBody(response);
            if (StringUtils.hasText(responseBody)) {
                logMessage.append(" | Response: ").append(truncateIfNeeded(responseBody));
            }
        }
        
        if (status >= 400) {
            log.warn(logMessage.toString());
        } else {
            log.info(logMessage.toString());
        }
    }

    private boolean hasBody(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content);
        }
        return "";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content);
        }
        return "";
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization") || 
               lowerCaseName.contains("cookie") || 
               lowerCaseName.contains("password") ||
               lowerCaseName.contains("token");
    }

    private boolean isLoggableResponseType(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        return contentType != null && 
               (contentType.contains("application/json") || 
                contentType.contains("text/") ||
                contentType.contains("application/xml"));
    }

    private String truncateIfNeeded(String content) {
        if (content.length() > MAX_PAYLOAD_LENGTH) {
            return content.substring(0, MAX_PAYLOAD_LENGTH) + "... [truncated]";
        }
        return content;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // No logar recursos estáticos o health checks
        return path.startsWith("/static/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") ||
               path.equals("/actuator/health");
    }
}
