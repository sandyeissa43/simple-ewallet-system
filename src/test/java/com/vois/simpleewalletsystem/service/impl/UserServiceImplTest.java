package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.generated.UserRequest;
import com.vois.simpleewalletsystem.dto.generated.UserResponse;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.exception.DuplicateEmailException;
import com.vois.simpleewalletsystem.exception.UserNotFoundException;
import com.vois.simpleewalletsystem.mapper.UserMapper;
import com.vois.simpleewalletsystem.repository.UserRepository;
import com.vois.simpleewalletsystem.service.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WalletService walletService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;


    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    private void setUpSecurityContext(String email) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }


    @Test
    void shouldCreateUserSuccessfully() {

        UserRequest request = new UserRequest()
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("12345678")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER);

        User savedUser = User.builder()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("encodedPassword")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        UserResponse expectedResponse = new UserResponse()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER)
                .active(true);

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(userMapper.toResponse(savedUser))
                .thenReturn(expectedResponse);

        UserResponse actualResponse =
                userService.createUser(request);

        assertEquals(
                expectedResponse.getId(),
                actualResponse.getId()
        );

        assertEquals(
                expectedResponse.getFullName(),
                actualResponse.getFullName()
        );

        assertEquals(
                expectedResponse.getEmail(),
                actualResponse.getEmail()
        );

        assertEquals(
                expectedResponse.getRole(),
                actualResponse.getRole()
        );

        assertEquals(
                expectedResponse.getActive(),
                actualResponse.getActive()
        );

        verify(userRepository)
                .existsByEmail(request.getEmail());

        verify(passwordEncoder)
                .encode(request.getPassword());

        verify(userRepository)
                .save(any(User.class));

        verify(walletService)
                .createWallet(savedUser);

        verify(userMapper)
                .toResponse(savedUser);
    }


    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        UserRequest request = new UserRequest()
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("12345678")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER);

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository)
                .existsByEmail(request.getEmail());

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(walletService);
        verifyNoInteractions(userMapper);
    }


    @Test
    void shouldGetUserByIdSuccessfully() {

        setUpSecurityContext("sandy@gmail.com");

        User user = User.builder()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .password("encodedPassword")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        User currentUser = User.builder()
                .id(1L)
                .email("sandy@gmail.com")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        UserResponse expectedResponse = new UserResponse()
                .id(1L)
                .fullName("Sandy Eissa")
                .email("sandy@gmail.com")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER)
                .active(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("sandy@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(userMapper.toResponse(user))
                .thenReturn(expectedResponse);

        UserResponse actualResponse =
                userService.getUserById(1L);

        assertEquals(
                expectedResponse.getId(),
                actualResponse.getId()
        );

        assertEquals(
                expectedResponse.getEmail(),
                actualResponse.getEmail()
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail("sandy@gmail.com");

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        // No SecurityContext is needed here.
        // findById() returns empty before getCurrentUser() is called.

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .findByEmail(anyString());

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

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .findByEmail(anyString());

        verifyNoInteractions(userMapper);
    }


    @Test
    void shouldUpdateUserSuccessfully() {

        setUpSecurityContext("sandy@gmail.com");

        UserRequest request = new UserRequest()
                .fullName("New Name")
                .email("new@gmail.com")
                .password("12345678")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER);

        User user = User.builder()
                .id(1L)
                .fullName("Old Name")
                .email("old@gmail.com")
                .password("oldEncodedPassword")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        User currentUser = User.builder()
                .id(1L)
                .email("sandy@gmail.com")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        UserResponse expectedResponse = new UserResponse()
                .id(1L)
                .fullName("New Name")
                .email("new@gmail.com")
                .active(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("sandy@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("newEncodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(expectedResponse);

        UserResponse actualResponse =
                userService.updateUser(1L, request);

        assertEquals(
                expectedResponse.getFullName(),
                actualResponse.getFullName()
        );

        assertEquals(
                expectedResponse.getEmail(),
                actualResponse.getEmail()
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail("sandy@gmail.com");

        verify(userRepository)
                .existsByEmail(request.getEmail());

        verify(passwordEncoder)
                .encode(request.getPassword());

        verify(userRepository)
                .save(user);

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {

        // No SecurityContext is needed.
        // findById() fails before getCurrentUser() is called.

        UserRequest request = new UserRequest()
                .fullName("New Name")
                .email("new@gmail.com")
                .password("12345678")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(1L, request)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .findByEmail(anyString());

        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }


    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {

        setUpSecurityContext("sandy@gmail.com");

        UserRequest request = new UserRequest()
                .fullName("New Name")
                .email("new@gmail.com")
                .password("12345678")
                .role(com.vois.simpleewalletsystem.dto.generated.Role.USER);

        User user = User.builder()
                .id(1L)
                .email("old@gmail.com")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        User currentUser = User.builder()
                .id(1L)
                .email("sandy@gmail.com")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("sandy@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(userRepository.existsByEmail("new@gmail.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.updateUser(1L, request)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail("sandy@gmail.com");

        verify(userRepository)
                .existsByEmail("new@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userMapper);
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

        assertFalse(user.getActive());

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .save(user);
    }


    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistingUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deactivateUser(1L)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void shouldReturnAllActiveUsers() {

        User user = User.builder()
                .id(1L)
                .fullName("Sandy")
                .role(com.vois.simpleewalletsystem.enums.Role.USER)
                .active(true)
                .build();

        UserResponse response = new UserResponse()
                .id(1L)
                .fullName("Sandy")
                .active(true);

        when(userRepository.findByActiveTrue())
                .thenReturn(List.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        List<UserResponse> users =
                userService.getAllUsers();

        assertEquals(1, users.size());

        assertEquals(
                "Sandy",
                users.get(0).getFullName()
        );

        verify(userRepository)
                .findByActiveTrue();

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldActivateUserSuccessfully() {

        User user = User.builder()
                .id(1L)
                .active(false)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.activateUser(1L);

        assertEquals(true, user.getActive());

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .save(user);
    }


    @Test
    void shouldThrowExceptionWhenActivatingNonExistingUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.activateUser(1L)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }
}

