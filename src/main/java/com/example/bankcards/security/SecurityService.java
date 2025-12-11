package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;

    public boolean isCurrentUser(Long userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("User not authenticated");
        }
        return currentUser.getId().equals(userId);
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserNotFoundException("User not authenticated");
        }

        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }
}
