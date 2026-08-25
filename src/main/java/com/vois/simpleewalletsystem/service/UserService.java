package com.vois.simpleewalletsystem.service;
import com.vois.simpleewalletsystem.dto.generated.UserRequest;
import com.vois.simpleewalletsystem.dto.generated.UserResponse;
import java.util.List;
import com.example.model.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.ChangePasswordRequest;

public interface UserService {

    UserResponse createUser(UserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest request);
    void deactivateUser(Long id);
    void activateUser(Long id);
    UserResponse getCurrentUser();
    void changePassword(ChangePasswordRequest request);
}

