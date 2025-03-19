package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getById(Long userId);

    UserDto create(User user);

    UserDto update(UserDto user, Long userId);

    void delete(Long userId);
}
