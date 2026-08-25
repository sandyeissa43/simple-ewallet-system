package com.vois.simpleewalletsystem.controller;
import com.vois.simpleewalletsystem.dto.generated.UserRequest;
import com.vois.simpleewalletsystem.dto.generated.UserResponse;
import com.vois.simpleewalletsystem.dto.request.ChangePasswordRequest;
import com.vois.simpleewalletsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
        name = "Users",
        description = "User management APIs"
)
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Create a new user",
            description = "Creates a new user with the provided information"
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users"
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user by their unique ID"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        UserResponse response =
                userService.getUserById(id);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update user",
            description = "Updates an existing user's information"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {

        UserResponse response =
                userService.updateUser(id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Deactivate user",
            description = "Deactivates an active user by their ID"
    )
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(
            @PathVariable Long id) {

        userService.deactivateUser(id);

        return ResponseEntity.ok(
                "User deactivated successfully"
        );
    }

    @Operation(
            summary = "Activate user",
            description = "Activates a deactivated user by their ID"
    )
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(
            @PathVariable Long id) {

        userService.activateUser(id);

        return ResponseEntity.ok(
                "User activated successfully"
        );
    }
    @Operation(
            summary = "Get current user",
            description = "Retrieves the currently authenticated user's information"
    )
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getCurrentUser() {

        UserResponse response =
                userService.getCurrentUser();

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/me/password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }



}