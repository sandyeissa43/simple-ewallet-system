package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.request.UserRequest;
import com.vois.simpleewalletsystem.dto.response.UserResponse;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.enums.Role;
import com.vois.simpleewalletsystem.exception.DuplicateEmailException;
import com.vois.simpleewalletsystem.exception.UserNotFoundException;
import com.vois.simpleewalletsystem.mapper.UserMapper;
import com.vois.simpleewalletsystem.repository.UserRepository;
import com.vois.simpleewalletsystem.service.UserService;
import com.vois.simpleewalletsystem.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {

        log.info("Creating user with email {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        walletService.createWallet(savedUser);

        log.info("User created successfully with id {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));

        if (!user.getActive()) {
            throw new UserNotFoundException("User is deactivated");
        }

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateEmailException("Email already exists");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with id {}", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));

        user.setActive(false);

        userRepository.save(user);

        log.info("User deactivated successfully with id {}", id);
    }

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findByActiveTrue()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public void activateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));

        user.setActive(true);

        userRepository.save(user);

        log.info("User activated successfully with id {}", id);
    }
}