package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.model.User;
import com.codigozerocuatro.taska.infra.dto.ProfileResponse;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profile/me")
public class ProfileController {

    @GetMapping
    public ResponseEntity<ProfileResponse> getUser(Authentication authentication) {
        User userAuthorized = (User) authentication.getPrincipal();
        UserEntity user = userAuthorized.getUser();
        ProfileResponse response = new ProfileResponse(
                user.getUsername(),
                user.getNombre(),
                user.isAdmin(),
                user.getPuesto().toString()
        );
        return ResponseEntity.ok().body(response);
    }

}
