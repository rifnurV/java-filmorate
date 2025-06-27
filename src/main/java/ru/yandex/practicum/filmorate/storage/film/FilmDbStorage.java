package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.like.LikeDao;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final LikeDao likeDao;
    private final UserStorage userStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, LikeDao likeDao, @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.likeDao = likeDao;
        this.userStorage = userStorage;
    }

    @Override
    public Film getFilmById(long filmId) {
        log.debug("getFilmById({})", filmId);
        try {
            Film thisFilm = jdbcTemplate.queryForObject(
                    "SELECT film_id, name, description, release_date, duration, mpa_id FROM films WHERE film_id=?",
                    new FilmMapper(), filmId);
            log.trace("The movie {} was returned", thisFilm);
            return thisFilm;
        } catch (EmptyResultDataAccessException e) {
            log.warn("No film found with id {}", filmId);
            throw new NotFoundException("Film with id " + filmId + " not found");
        }
    }

    @Override
    public Film createFilm(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenres(film.getId(), film.getGenres());
        }
        log.info("Film {} created", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("Update Film({}).", film);

        if (film.getId() <= 0 || !isContains(film.getId())) {
            throw new NotFoundException("Attempt to update non-existing movie with id " + film.getId());
        }
        String query = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? where film_id = ?";
        jdbcTemplate.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        Film thisFilm = getFilmById(film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            updateGenres(thisFilm.getId(), uniqueGenres);
        }

        log.trace("The movie {} was updated in the database", thisFilm);
        return thisFilm;
    }

    @Override
    public List<Film> findAllFilms() {
        log.debug("Execute findAll()");
        String query = "SELECT film_id, name, description, release_date, duration, mpa_id FROM films";
        List<Film> films = jdbcTemplate.query(query, new FilmMapper());
        return films;
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        List<Film> popularMovies = findAllFilms()
                .stream()
                .sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
        log.trace("Popular films: {}", popularMovies);
        return popularMovies;
    }

    @Override
    public void addLikeFilms(long filmId, long userId) {
        if (filmId < 0 || userId < 0) {
            throw new IllegalArgumentException("Film ID and user ID cannot be null");
        }

        Film film = getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Film " + filmId + " not found");
        }

        if (!userStorage.userExists(userId)) {
            throw new NotFoundException("User " + userId + " not found");
        }

        if (!likeDao.isLiked(filmId, userId)) {
            likeDao.like(filmId, userId);
            log.info("User {} added a like to the movie {}", userId, filmId);
        } else {
            log.info("User {} liked the movie with the ID {}", userId, filmId);
        }
    }

    @Override
    public void deleteLikeFilms(long filmId, long userId) {
        if (filmId < 0 || userId < 0) {
            throw new IllegalArgumentException("The movie and the user cannot be null");
        }

        Film film = getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Film " + filmId + " не not found");
        }

        if (!userStorage.userExists(userId)) {
            throw new NotFoundException("User " + userId + " not found");
        }

        if (likeDao.isLiked(filmId, userId)) {
            likeDao.dislike(filmId, userId);
            log.info("User {} disliked the movie {}", userId, filmId);
        } else {
            throw new IllegalArgumentException("The user did not like the movie {}" + filmId);
        }

    }

    @Override
    public boolean isContains(long id) {
        log.debug("isContains({})", id);
        try {
            getFilmById(id);
            log.trace("Film {} was found", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("Not found for id {}", id);
            return false;
        }
    }

    @Override
    public Set<Genre> getGenres(long filmId) {
        log.debug("getGenres({})", filmId);
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(
                "SELECT fg.genre_id, g.name FROM film_genres fg " +
                        "LEFT OUTER JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id=? ORDER BY g.genre_id",
                new GenreMapper(), filmId));
        log.trace("Genres for filmid {} returned", filmId);
        return genres;
    }

    private void addGenres(long filmId, Set<Genre> genres) {
        log.debug("addGenres({}, {})", filmId, genres);

        Set<Genre> uniqueGenres = new HashSet<>();

        for (Genre genre : genres) {
            if (uniqueGenres.add(genre)) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
                log.trace("Genre {} was added to movie {}", genre.getName(), filmId);
            } else {
                log.trace("Duplicate genre {} found in input and will not be added", genre.getName());
            }
        }
    }

    private void updateGenres(long filmId, Set<Genre> genres) {
        log.debug("updateGenres({}, {})", filmId, genres);
        deleteGenres(filmId);
        addGenres(filmId, genres);
    }

    private void deleteGenres(long filmId) {
        log.debug("deleteGenres({})", filmId);
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id=?", filmId);
        log.trace("All genres were removed for a movie with id {}", filmId);
    }

    private int compare(Film film, Film otherFilm) {
        return Integer.compare(likeDao.countLikes(otherFilm.getId()), likeDao.countLikes(film.getId()));
    }
}
