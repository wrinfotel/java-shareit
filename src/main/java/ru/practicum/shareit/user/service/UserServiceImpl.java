package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto create(User user) {
        boolean checkUser = getAllUsers().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));
        if (checkUser) {
            throw new ConflictException("Этот имейл уже используется");
        }
        User createdUser = userRepository.create(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(UserDto user, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (user.getEmail() != null) {
            boolean checkUser = getAllUsers().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));
            if (checkUser) {
                throw new ConflictException("Этот имейл уже используется");
            }
        }
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setId(oldUser.getId());
        User updatedUser = userRepository.update(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long userId) {
        getById(userId);
        userRepository.delete(userId);
    }
}
