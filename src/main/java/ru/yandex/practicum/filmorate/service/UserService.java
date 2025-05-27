package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class UserService {

    private final InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public List<User> findAllFriends(long userId) {
        if (userId < 0) {
            throw new NotFoundException("userId is null or empty");
        }
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь не существует");
        }
        List<User> userList = new ArrayList<>();

        for (Long friendId : userStorage.findById(userId).get().getFriends()) {
            userList.add(userStorage.findById(friendId).get());
        }
        return userList;
    }

    public Optional<User> findById(long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (userStorage.findById(user.getId()) == null || user.getId() < 0) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        validateUser(user);
        return userStorage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        if (userId < 0 || friendId < 0) {
            throw new NotFoundException("Не указаны userId");
        }
        if (userStorage.findById(userId).isPresent() && userStorage.findById(friendId).isPresent()) {
            userStorage.addFriend(userId, friendId);
        } else {
            throw new NotFoundException("Не верный запрос");
        }
    }

    public void deleteFriends(long userId, long friendId) {
        if (userId < 0 || friendId < 0) {
            throw new ValidationException("Не корректные параметры");
        }
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Такого пользователя нет ");
        }
        if (!userStorage.findById(userId).get().getFriends().contains(friendId) && userStorage.findById(friendId).isEmpty()) {
            throw new NotFoundException("У пользователя нет друга");
        }
        userStorage.deleteFriends(userId, friendId);
    }

    public List<User> findAllFriendsCommon(long userId, long otherId) {
        return userStorage.findAllFriendsCommon(userId, otherId);
    }

    private void validateUser(User user) {

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Ошибка, дата рождения не может быть больше текущей даты");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ValidationException("Ошибка, email не может быть пустым");
        }

        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            throw new ValidationException("Ошибка валидации пользователя по логину");
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

    }

}
