package com.vois.simpleewalletsystem.mapper;

import com.vois.simpleewalletsystem.dto.generated.Role;
import com.vois.simpleewalletsystem.dto.generated.UserResponse;
import com.vois.simpleewalletsystem.entity.User;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return new UserResponse()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(Role.valueOf(user.getRole().name()))
                .active(user.getActive())
                .createdAt(user.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(user.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }
}