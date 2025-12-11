package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User createTestUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(role);
        return user;
    }

    private UserDto createTestUserDto(Long id, Role role) {
        return UserDto.builder()
                .id(id)
                .username("testuser")
                .email("test@example.com")
                .role(role)
                .build();
    }

    @Test
    void getCurrentUserInfo_ShouldReturnCurrentUser() {
        User user = createTestUser(1L, Role.ROLE_USER);
        UserDto userDto = createTestUserDto(1L, Role.ROLE_USER);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password")
                .roles("USER")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDto);

        UserDto result = userService.getCurrentUserInfo();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getCurrentUserInfo_WhenUserNotFound_ShouldThrowException() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("unknown")
                .password("password")
                .roles("USER")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUserInfo());
    }

    @Test
    void getAllUsers_ShouldReturnUsersPage() {
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = createTestUser(1L, Role.ROLE_USER);
        User user2 = createTestUser(2L, Role.ROLE_ADMIN);
        Page<User> usersPage = new PageImpl<>(Arrays.asList(user1, user2));

        when(userRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(usersPage);
        when(userMapper.toDTO(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return createTestUserDto(user.getId(), user.getRole());
        });

        Page<UserDto> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(userRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        User user = createTestUser(1L, Role.ROLE_USER);
        UserDto userDto = createTestUserDto(1L, Role.ROLE_USER);

        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findByIdWithCards(1L);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void updateUserRole_WhenValidData_ShouldUpdateRole() {
        User user = createTestUser(1L, Role.ROLE_USER);
        User savedUser = createTestUser(1L, Role.ROLE_ADMIN);
        UserDto userDto = createTestUserDto(1L, Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(userDto);

        UserDto result = userService.updateUserRole(1L, Role.ROLE_ADMIN);

        assertNotNull(result);
        assertEquals(Role.ROLE_ADMIN, result.getRole());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_WhenSameRole_ShouldThrowException() {
        User user = createTestUser(1L, Role.ROLE_USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.updateUserRole(1L, Role.ROLE_USER));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserRole(1L, Role.ROLE_ADMIN));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsersByRole_ShouldReturnUsersList() {
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = createTestUser(1L, Role.ROLE_USER);
        User user2 = createTestUser(2L, Role.ROLE_USER);
        Page<User> usersPage = new PageImpl<>(Arrays.asList(user1, user2));

        when(userRepository.findByRole(Role.ROLE_USER, pageable)).thenReturn(usersPage);
        when(userMapper.toDTO(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return createTestUserDto(user.getId(), user.getRole());
        });

        var result = userService.getUsersByRole(Role.ROLE_USER, pageable);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByRole(Role.ROLE_USER, pageable);
    }

    @Test
    void deleteUser_WhenUserHasNoCards_ShouldDeleteUser() {
        User user = createTestUser(1L, Role.ROLE_USER);
        user.setCards(Collections.emptyList());

        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_WhenUserHasCards_ShouldThrowException() {
        User user = createTestUser(1L, Role.ROLE_USER);
        user.setCards(Arrays.asList(new com.example.bankcards.entity.BankCard()));

        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any(User.class));
    }
}
