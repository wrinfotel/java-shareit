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
import java.util.Optional;

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
    public UserDto create(UserDto user) {
        boolean checkUser = getAllUsers().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));
        if (checkUser) {
            throw new ConflictException("Этот имейл уже используется");
        }

        User createdUser = userRepository.save(UserMapper.toUser(user));
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (userDto.getEmail() != null) {
            boolean checkUser = getAllUsers().stream().anyMatch(user1 -> user1.getEmail().equals(userDto.getEmail()));
            if (checkUser) {
                throw new ConflictException("Этот имейл уже используется");
            }
        }
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        User user = UserMapper.toUser(userDto);
        User updatedUser = userRepository.save(updateUserFields(oldUser, user));
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(userRepository::delete);
    }

    private User updateUserFields(User oldUser, User user) {
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        return oldUser;
    }
}
