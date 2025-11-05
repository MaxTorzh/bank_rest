package com.example.bankcards.service.user;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Page<UserDto> getAllUsers(Pageable pageable);

    UserDto updateUserRole(Long userId, Role role);

    List<UserDto> getUsersByRole(Role role, Pageable pageable);

    void deleteUser(Long userId);

    UserDto getUserById(Long userId);

    UserDto getCurrentUserInfo();

    User getCurrentUserEntity();

    User getUserByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User createUser(User user);
}
