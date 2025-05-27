package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private int nextId = 1;

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(@Valid @RequestBody User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен", user);
        return user;
    }

    @Override
    public User update(@Valid @RequestBody User newUser) {
        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        users.put(newUser.getId(), newUser);
        log.info("Пользователь {} обновлен", newUser);
        return newUser;


    }

    @Override
    public List<User> findAll() {
        List<User> usersList = new ArrayList<>(users.values());
        return usersList;
    }

    @Override
    public List<User> findAllFriendsCommon(long userId, long otherId) {
        ArrayList<User> commonFriendsArrayList = new ArrayList<>();
        if (users.get(userId) == null || users.get(otherId) == null) {
            return commonFriendsArrayList;
        }
        if (users.get(userId).getFriends() == null || users.get(otherId).getFriends() == null) {
            return commonFriendsArrayList;
        }
        for (Long i : users.get(userId).getFriends()) {
            for (Long j : users.get(otherId).getFriends()) {
                if (i == j) {
                    commonFriendsArrayList.add(users.get(i));
                }
            }
        }
        return commonFriendsArrayList;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        users.get(userId).getFriends().add(friendId);
        users.get(friendId).getFriends().add(userId);
    }

    @Override
    public void deleteFriends(long userId, long friendId) {
        users.get(userId).getFriends().remove(friendId);
        users.get(friendId).getFriends().remove(userId);
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        return nextId++;
    }
}
