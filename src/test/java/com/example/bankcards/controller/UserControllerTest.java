package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.config.TestConfig;
import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, TestConfig.class, TestSecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityService securityService;

    private UserDto createTestUserDto(Long id, Role role) {
        return UserDto.builder()
                .id(id)
                .username("user" + id)
                .email("user" + id + "@example.com")
                .role(role)
                .createdAt(OffsetDateTime.now())
                .cardsCount(2)
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUser_ShouldReturnUserInfo() throws Exception {
        UserDto userDto = createTestUserDto(1L, Role.ROLE_USER);

        when(userService.getCurrentUserInfo()).thenReturn(userDto);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.cardsCount").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_AsAdmin_ShouldReturnUser() throws Exception {
        UserDto userDto = createTestUserDto(1L, Role.ROLE_USER);

        when(userService.getUserById(1L)).thenReturn(userDto);
        when(securityService.isCurrentUser(1L)).thenReturn(false);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersByRole_AsAdmin_ShouldReturnFilteredUsers() throws Exception {
        UserDto userDto = createTestUserDto(1L, Role.ROLE_USER);

        when(userService.getUsersByRole(eq(Role.ROLE_USER), any(Pageable.class)))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/users/role/ROLE_USER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("ROLE_USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_AsAdmin_ShouldUpdateRole() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .username("user1")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userService.updateUserRole(1L, Role.ROLE_ADMIN)).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/1/role")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AsAdmin_ShouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserRole_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/api/users/1/role")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().isForbidden());
    }
}
