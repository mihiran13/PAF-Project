package com.smartcampus.service;

import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.UserResponse;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public User getUserByOauthId(String oauthId) {
        return userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "oauthId", oauthId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> usersPage = userRepository.findAll(PageRequest.of(page, size));
        Page<UserResponse> responsePage = usersPage.map(UserResponse::fromEntity);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsersByRole(UserRole role, int page, int size) {
        Page<User> usersPage = userRepository.findByRole(role, PageRequest.of(page, size));
        Page<UserResponse> responsePage = usersPage.map(UserResponse::fromEntity);
        return PagedResponse.from(responsePage);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }
}
