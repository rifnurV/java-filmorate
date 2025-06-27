package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.mapper.FriendshipMapper;

import java.util.List;

@Slf4j
@Repository
public class FriendshipDaoImpl implements FriendshipDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FriendshipDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(long userId, long friendId, boolean isFriend) {
        log.debug("addFriend({}, {}, {})", userId, friendId, isFriend);
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id, is_friend) VALUES(?, ?, ?)",
                userId, friendId, isFriend);
        Friendship friendship = getFriend(userId, friendId);
        log.trace("These users are friends now: {}", friendship);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        log.debug("Удаление дружбы: пользователь ID={}, друг ID={}", userId, friendId);

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId, friendId);

        if (rowsAffected == 0) {
            log.warn("Couldn't delete friendship between users {} and {}: record not found", userId, friendId);
            return;
        }

        log.info("Friendship between users {} and {} removed", userId, friendId);
    }

    @Override
    public List<Integer> getFriends(long userId) {
        log.debug("The user's friends {} ", userId);
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ? AND is_friend = true";
        List<Integer> friendsList = jdbcTemplate.queryForList(sql, Integer.class, userId);
        log.info("The user's friends {}: {}", userId, friendsList);
        return friendsList;
    }

    @Override
    public Friendship getFriend(long userId, long friendId) {
        log.debug("Friendship between {} and {} users", userId, friendId);
        String sql = "SELECT user_id, friend_id, is_friend FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new FriendshipMapper(), userId, friendId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Friendship between {} and {} users not found", userId, friendId);
            return null;
        }
    }
}