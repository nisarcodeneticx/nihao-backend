package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.exception.BadRequestException;
import com.codeneticx.nihaobackend.model.User;
import com.codeneticx.nihaobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
