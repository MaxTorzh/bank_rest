package com.example.bankcards.service.user;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        return userRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(Long userId, Role role) {
        log.info("Updating role for user ID: {} to {}", userId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (user.getRole() == role) {
            throw new BadRequestException("User already has role: " + role);
        }

        user.setRole(role);
        User savedUser = userRepository.save(user);
        log.info("User role updated successfully: {} -> {}", userId, role);

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findByIdWithCards(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toDTO)
                .getContent();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findByIdWithCards(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!user.getCards().isEmpty()) {
            throw new BadRequestException("Cannot delete user with active cards");
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserInfo() {
        User user = getCurrentUserEntity();
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
}
