package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipDao;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendshipDao friendshipDao;

    @Override
    public Optional<User> findById(long userId) {
        try {
            User findUser = jdbcTemplate.queryForObject(
                    "SELECT id, email, login, name, birthday FROM users WHERE id=?",
                    new UserMapper(), userId);
            log.info("Found user with id {}", userId);
            return Optional.of(findUser);
        } catch (EmptyResultDataAccessException e) {
            log.error("User with id {} not found", userId);
            return Optional.empty();
        }
    }

    @Override
    public User create(@Valid @RequestBody User user) {
        log.info("Creating new user {}", user.getName());

        if (userExistsByEmail(user.getEmail())) {
            log.warn("Email {} is already in use", user.getEmail());
            throw new ValidationException("Email is already in use");
        }
        if (userExistsByLogin(user.getLogin())) {
            log.warn("Login {} is already in use", user.getLogin());
            throw new ValidationException("Login is already in use");
        }
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName() != null && !user.getName().isEmpty() ? user.getName() : user.getLogin(),
                Date.valueOf(user.getBirthday()));

        User newUser = jdbcTemplate.queryForObject(
                "SELECT id, email, login, name, birthday FROM users WHERE login=?",
                new UserMapper(), user.getLogin());

        log.info("User {} added", newUser);
        return newUser;
    }

    @Override
    public User update(@Valid @RequestBody User newUser) {
        if (!userExists(newUser.getId())) {
            log.warn("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        if (userExistsByLogin(newUser.getLogin(), newUser.getId())) {
            log.warn("Логин {} уже используется", newUser.getLogin());
            throw new ValidationException("Логин уже используется");
        }
        if (userExistsByEmail(newUser.getEmail(), newUser.getId())) {
            log.warn("Электронная почта {} уже используется", newUser.getEmail());
            throw new ValidationException("Электронная почта уже используется");
        }
        jdbcTemplate.update("UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?",
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId());


        User updatedUser = findById(newUser.getId()).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("Update user: {}", updatedUser);
        return updatedUser;

    }

    @Override
    public List<User> findAll() {
        log.info("Find all users");
        List<User> users = jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users",
                new UserMapper());
        log.info("Users count: {}", users.size());
        return users;
    }

    @Override
    public boolean userExists(long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id=?", Integer.class, userId);
        return count > 0;
    }

    @Override
    public List<User> findAllFriends(long userId) {
        findById(userId).orElseThrow(() -> new NotFoundException("User from ID " + userId + " not found"));

        List<Integer> friendIds = friendshipDao.getFriends(userId);
        List<User> friendsList = new ArrayList<>();
        for (Integer friendId : friendIds) {
            User friend = findById(friendId).orElse(null);
            if (friend != null) {
                friendsList.add(friend);
            }
        }

        log.info("Friends users from ID {}: {}", userId, friendsList);
        return friendsList;
    }

    @Override
    public List<User> findAllFriendsCommon(long userId, long otherId) {
        findById(userId).orElseThrow(() -> new NotFoundException("User from ID " + userId + " not found"));
        findById(otherId).orElseThrow(() -> new NotFoundException("User from ID " + otherId + " not found"));

        List<Integer> friendsUser1 = friendshipDao.getFriends(userId);
        List<Integer> friendsUser2 = friendshipDao.getFriends(otherId);

        List<User> commonFriends = friendsUser1.stream()
                .filter(friendsUser2::contains)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        log.info("Общие друзья между пользователями {} и {}: {}", userId, otherId, commonFriends);
        return commonFriends;
    }

    private boolean userExistsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count > 0;
    }

    private boolean userExistsByEmail(String email, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?", Integer.class, email, userId);
        return count > 0;
    }

    private boolean userExistsByLogin(String login) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE login = ?", Integer.class, login);
        return count > 0;
    }

    private boolean userExistsByLogin(String login, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE login = ? AND id != ?", Integer.class, login, userId);
        return count > 0;
    }

}
