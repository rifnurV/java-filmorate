package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.mapper.LikeMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeDaoImpl implements LikeDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void like(long filmId, long userId) {
        log.debug("like({}, {})", filmId, userId);
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        log.trace("The movie {} was liked by user {}", filmId, userId);
    }

    @Override
    public void dislike(long filmId, long userId) {
        log.debug("dislike({}, {})", filmId, userId);
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id=? AND user_id=?", filmId, userId);
        log.trace("The user {}, disliked the movie {}", userId, filmId);
    }

    @Override
    public int countLikes(long filmId) {
        log.debug("countLikes({}).", filmId);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id=?", Integer.class, filmId);

        if (count == null) {
            log.warn("No likes found for movie {}", filmId);
            return 0;
        }

        log.trace("The movie {} liked {} times", filmId, count);
        return count;
    }

    @Override
    public boolean isLiked(long filmId, long userId) {
        log.debug("isLiked({}, {})", filmId, userId);
        try {
            jdbcTemplate.queryForObject("SELECT film_id, user_id FROM film_likes WHERE film_id=? AND user_id=?",
                    new LikeMapper(), filmId, userId);
            log.trace("The movie {} was liked by user {}", filmId, userId);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("There is no like for film {} from user {}", filmId, userId);
            return false;
        }
    }
}
