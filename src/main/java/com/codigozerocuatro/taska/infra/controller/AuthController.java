package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.model.User;
import com.codigozerocuatro.taska.infra.config.AppProperties;
import com.codigozerocuatro.taska.infra.dto.ErrorResponse;
import com.codigozerocuatro.taska.infra.dto.LoginReponse;
import com.codigozerocuatro.taska.infra.dto.LoginRequest;
import com.codigozerocuatro.taska.infra.dto.ProfileResponse;
import com.codigozerocuatro.taska.infra.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String COOKIE_NAME_REFRESH_TOKEN = "refresh_token";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AppProperties appProperties;

    @PostMapping("/login")
    public ResponseEntity<LoginReponse> login(@Valid @RequestBody LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtService.generateToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);

        var userAuthorized = (User) auth.getPrincipal();
        var user = userAuthorized.getUser();
        var response = new LoginReponse(token, refreshToken,
                new ProfileResponse(
                        user.getUsername(),
                        user.getNombre(),
                        user.isAdmin(),
                        user.getPuesto().toString()
                ));

        return ResponseEntity.ok()
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = COOKIE_NAME_REFRESH_TOKEN, required = false) String refreshToken) {
        log.debug("Refresh token: {}", refreshToken);

        if (refreshToken == null || refreshToken.isBlank() || jwtService.isNotRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "Refresh token is invalid"));
        }

        String username = jwtService.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        var auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String newToken = jwtService.generateToken(auth);
        String newRefresh = jwtService.generateRefreshToken(auth);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE)
                .body(new LoginReponse(newToken, newRefresh, null));

    }

}
