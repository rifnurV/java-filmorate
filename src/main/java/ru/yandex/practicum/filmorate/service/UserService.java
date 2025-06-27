package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipDao;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipDao friendshipDao;

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, FriendshipDao friendshipDao) {
        this.userStorage = userStorage;
        this.friendshipDao = friendshipDao;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public List<User> findAllFriends(long userId) {
        return userStorage.findAllFriends(userId);
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
        return userStorage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        log.info("Add friend {} to user {}", friendId, userId);

        if (userId < 0 || friendId < 0) {
            throw new NotFoundException("Not specified userId");
        }

        findById(userId).orElseThrow(() -> {
            log.warn("User {} not found", userId);
            throw new NotFoundException("User not found");
        });

        findById(friendId).orElseThrow(() -> {
            log.warn("Friend {} not found", friendId);
            throw new NotFoundException("Friend not found");
        });

        friendshipDao.addFriend(userId, friendId, true);
        log.info("User {} has been added as a friend to the user {}", friendId, userId);
    }

    public void deleteFriends(long userId, long friendId) {
        if (userId < 0 || friendId < 0) {
            throw new ValidationException("Incorrect parameters");
        }
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("There is no such user");
        }
        if (!userStorage.findById(userId).get().getFriends().contains(friendId) && userStorage.findById(friendId).isEmpty()) {
            throw new NotFoundException("The user does not have a friend");
        }
        try {
            friendshipDao.deleteFriend(userId, friendId);
            log.info("Friendship between users {} and {} removed", userId, friendId);
        } catch (Exception e) {
            log.error("Error deleting friendship between users {} and {}}: {}", userId, friendId, e.getMessage());
            throw new RuntimeException("Friendship could not be deleted.", e);
        }
    }

    public List<User> findAllFriendsCommon(long userId, long otherId) {
        return userStorage.findAllFriendsCommon(userId, otherId);
    }

    private void validateUser(User user) {

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("The date of birth cannot be more than the current date.");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }

        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            throw new ValidationException("Invalid login");
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

    }

}
