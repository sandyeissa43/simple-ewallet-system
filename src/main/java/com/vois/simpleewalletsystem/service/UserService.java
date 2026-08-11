package com.vois.simpleewalletsystem.service;
import com.vois.simpleewalletsystem.dto.request.UserRequest;
import com.vois.simpleewalletsystem.dto.response.UserResponse;


public interface UserService {
    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);
}

