package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    User user1 = User.builder()
            .id(1L)
            .email("test@user.ru")
            .name("User 1")
            .build();

    User user2 = User.builder()
            .id(2L)
            .email("test2@user.ru")
            .name("User 2")
            .build();

    @Test
    void shouldGetAllUsers() {
        List<User> listUsers = new ArrayList<>();
        listUsers.add(user1);
        listUsers.add(user2);
        Mockito.when(userRepository.findAll()).thenReturn(listUsers);

        List<UserDto> testGetUsers = userService.getAllUsers();
        Assertions.assertEquals(2, testGetUsers.size());
        Assertions.assertEquals(listUsers.stream().map(UserMapper::toUserDto).toList(), testGetUsers);
        verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    void shouldGetUserById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        UserDto testGetUser = userService.getById(1L);
        Assertions.assertEquals(UserMapper.toUserDto(user1), testGetUser);
        verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void shouldNotGetUserById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> userService.getById(1L));
        Assertions.assertEquals("user not found", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void shouldCreateUser() {
        UserDto userDto = UserMapper.toUserDto(user1);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user1);
        UserDto savedUser = userService.create(userDto);
        Assertions.assertEquals(userDto, savedUser);
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void shouldNotCreateUser() {
        UserDto userDto = UserMapper.toUserDto(user1);
        List<User> listUsers = new ArrayList<>();
        listUsers.add(user1);
        listUsers.add(user2);
        Mockito.when(userRepository.findAll()).thenReturn(listUsers);
        final ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> userService.create(userDto));
        Assertions.assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    void shouldUpdateUser() {
        UserDto userDto = UserMapper.toUserDto(user1);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user1);
        UserDto savedUser = userService.update(userDto, 1L);
        Assertions.assertEquals(userDto, savedUser);
        verify(userRepository, Mockito.times(1)).save(any(User.class));
        verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void shouldNotUpdateUserConflict() {
        UserDto userDto = UserMapper.toUserDto(user1);
        List<User> listUsers = new ArrayList<>();
        listUsers.add(user1);
        listUsers.add(user2);
        Mockito.when(userRepository.findAll()).thenReturn(listUsers);
        final ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> userService.update(userDto, 1L));
        Assertions.assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    void shouldNotUpdateUserNotFound() {
        UserDto userDto = UserMapper.toUserDto(user1);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> userService.update(userDto, 1L));
        verify(userRepository, Mockito.times(1)).findById(1L);
        Assertions.assertEquals("user not found", exception.getMessage());
    }

    @Test
    void shouldDeleteUser() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user1));
        userService.delete(1L);
        verify(userRepository, Mockito.times(1)).delete(any(User.class));
    }
}
