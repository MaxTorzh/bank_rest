package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;

    public boolean isCurrentUser(Long userId) {
        try {
            User currentUser = getCurrentUser();
            return currentUser != null && currentUser.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public User getCurrentUser() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();
            String username;

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            return userService.getUserByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }
}
