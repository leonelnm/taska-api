package com.codigozerocuatro.taska.infra.controller;

import com.codigozerocuatro.taska.domain.model.User;
import com.codigozerocuatro.taska.domain.service.UserService;
import com.codigozerocuatro.taska.infra.dto.ChangePasswordRequest;
import com.codigozerocuatro.taska.infra.dto.ProfileResponse;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profile/me")
public class ProfileController {

    private final UserService userService;

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

    @PostMapping("password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User userAuthorized = (User) authentication.getPrincipal();
        UserEntity user = userAuthorized.getUser();
        userService.changePassword(user, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }


}
