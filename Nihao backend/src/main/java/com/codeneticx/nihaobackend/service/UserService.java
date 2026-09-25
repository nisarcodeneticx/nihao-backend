package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.dto.response.UserResponse;
import com.codeneticx.nihaobackend.exception.BadRequestException;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.mapper.AdminMapper;
import com.codeneticx.nihaobackend.model.User;
import com.codeneticx.nihaobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponse> getAll(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchUsers(emptyToNull(search), emptyToNull(status), pageable);
        Page<UserResponse> mapped = users.map(adminMapper::toUserResponse);
        return PaginatedResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(String id) {
        return adminMapper.toUserResponse(findUser(id));
    }

    @Transactional
    public UserResponse update(String id, Map<String, Object> updates) {
        User user = findUser(id);
        if (updates.containsKey("fullName")) {
            user.setFullName(String.valueOf(updates.get("fullName")));
        }
        if (updates.containsKey("role")) {
            user.setRole(String.valueOf(updates.get("role")).toUpperCase());
        }
        if (updates.containsKey("status")) {
            user.setStatus(String.valueOf(updates.get("status")).toUpperCase());
        }
        if (updates.containsKey("avatarUrl")) {
            user.setAvatarUrl(String.valueOf(updates.get("avatarUrl")));
        }
        userRepository.save(user);
        return adminMapper.toUserResponse(user);
    }

    @Transactional
    public void delete(String id) {
        User user = findUser(id);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Cannot delete admin user");
        }
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse ban(String id) {
        User user = findUser(id);
        user.setStatus("BANNED");
        userRepository.save(user);
        return adminMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse grantPremium(String id) {
        User user = findUser(id);
        // Premium flag is not persisted yet; acknowledge request for admin portal compatibility.
        userRepository.save(user);
        return adminMapper.toUserResponse(user);
    }

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
