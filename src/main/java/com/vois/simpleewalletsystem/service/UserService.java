package com.vois.simpleewalletsystem.service;
import com.vois.simpleewalletsystem.dto.request.UserRequest;
import com.vois.simpleewalletsystem.dto.response.UserResponse;
import java.util.List;
import com.example.model.DepositRequest;

public interface UserService {
    UserResponse createUser(UserRequest request);
    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserRequest request);


    void deactivateUser(Long id);
    void activateUser(Long id);
}

