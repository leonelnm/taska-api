package com.codigozerocuatro.taska.domain.service;

import com.codigozerocuatro.taska.domain.model.User;
import com.codigozerocuatro.taska.infra.persistence.model.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUtils {

    public UserEntity getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getAuthenticatedUser(authentication);
    }

    public UserEntity getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            log.debug("Principal is not an instance of UserDetails: {}", principal.getClass().getSimpleName());
            return null;
        }

        if (!(userDetails instanceof User customUserDetails)) {
            log.debug("UserDetails is not an instance of custom User class: {}", userDetails.getClass().getSimpleName());
            return null;
        }

        return customUserDetails.getUser();
    }

}
