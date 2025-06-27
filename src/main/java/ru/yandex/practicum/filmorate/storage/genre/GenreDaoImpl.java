package ru.yandex.practicum.filmorate.storage.genre;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.util.List;

@Slf4j
@Component
@Data
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenreById(long id) {
        Genre genre = jdbcTemplate.queryForObject("SELECT genre_id, name FROM genres WHERE genre_id=?",
                new GenreMapper(), id);
        log.trace("The genre type with id {} was returned", id);
        return genre;
    }

    @Override
    public List<Genre> getAllGenres() {
        log.debug("getGenres()");
        List<Genre> genreList = jdbcTemplate.query(
                "SELECT DISTINCT genre_id, name FROM genres ORDER BY genre_id",
                new GenreMapper()
        );
        log.trace("These are all unique genre types: {}", genreList);
        return genreList;
    }

    @Override
    public boolean isContains(int id) {
        log.debug("isContains({})", id);
        try {
            getGenreById(id);
            log.trace("The genre with id {} was found", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("No information for id {} was found", id);
            return false;
        }
    }

    @Override
    public List<Long> getAllGenreIds() {
        log.debug("getAllGenreIds()");
        List<Long> genreIds = jdbcTemplate.query(
                "SELECT DISTINCT genre_id FROM genres ORDER BY genre_id",
                (rs, rowNum) -> rs.getLong("genre_id")
        );
        log.trace("All genre IDs: {}", genreIds);
        return genreIds;
    }
}
