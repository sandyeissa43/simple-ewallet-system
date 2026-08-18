package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.request.UserRequest;
import com.vois.simpleewalletsystem.dto.response.UserResponse;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.enums.Role;
import com.vois.simpleewalletsystem.mapper.UserMapper;
import com.vois.simpleewalletsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.vois.simpleewalletsystem.exception.DuplicateEmailException;

import java.util.List;
import java.util.Optional;
import com.vois.simpleewalletsystem.exception.UserNotFoundException;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {

        UserRequest request = UserRequest.builder()
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("12345678")
                .role(Role.USER)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("12345678")
                .role(Role.USER)
                .active(true)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(userMapper.toResponse(savedUser))
                .thenReturn(expectedResponse);


        UserResponse actualResponse = userService.createUser(request);


        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getFullName(), actualResponse.getFullName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        assertEquals(expectedResponse.getRole(), actualResponse.getRole());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(savedUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        UserRequest request = UserRequest.builder()
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("12345678")
                .role(Role.USER)
                .build();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> userService.createUser(request));

        verify(userRepository).existsByEmail(request.getEmail());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(userMapper);
    }
    @Test
    void shouldGetUserByIdSuccessfully() {

        User user = User.builder()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("12345678")
                .active(true)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.getUserById(1L);

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }
    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));

        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }
    @Test
    void shouldThrowExceptionWhenUserIsDeactivated() {

        User user = User.builder()
                .id(1L)
                .active(false)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));

        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }
    @Test
    void shouldUpdateUserSuccessfully() {

        UserRequest request = UserRequest.builder()
                .fullName("New Name")
                .email("new@gmail.com")
                .password("12345678")
                .build();

        User user = User.builder()
                .id(1L)
                .fullName("Old Name")
                .email("old@gmail.com")
                .password("11111111")
                .active(true)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .fullName("New Name")
                .email("new@gmail.com")
                .active(true)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(expectedResponse);

        UserResponse actualResponse = userService.updateUser(1L, request);

        assertEquals(expectedResponse.getFullName(), actualResponse.getFullName());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {

        UserRequest request = UserRequest.builder().build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(1L, request));

        verify(userRepository).findById(1L);
    }
    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {

        UserRequest request = UserRequest.builder()
                .email("new@gmail.com")
                .build();

        User user = User.builder()
                .id(1L)
                .email("old@gmail.com")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new@gmail.com"))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> userService.updateUser(1L, request));

        verify(userRepository).existsByEmail("new@gmail.com");
    }
    @Test
    void shouldDeactivateUserSuccessfully() {

        User user = User.builder()
                .id(1L)
                .active(true)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertEquals(false, user.getActive());

        verify(userRepository).save(user);
    }
    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistingUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deactivateUser(1L));

        verify(userRepository).findById(1L);
    }
    @Test
    void shouldReturnAllActiveUsers() {

        User user = User.builder()
                .id(1L)
                .fullName("Sandy")
                .active(true)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .fullName("Sandy")
                .active(true)
                .build();

        when(userRepository.findByActiveTrue())
                .thenReturn(List.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        List<UserResponse> users = userService.getAllUsers();

        assertEquals(1, users.size());

        verify(userRepository).findByActiveTrue();
        verify(userMapper).toResponse(user);
    }

}