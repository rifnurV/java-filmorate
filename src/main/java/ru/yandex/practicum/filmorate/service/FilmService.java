package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.MPA.MPADao;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDao;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final MPADao mpaDao;
    private final GenreDao genreDao;

    public FilmService(@Qualifier("UserDbStorage") UserStorage userStorage, @Qualifier("FilmDbStorage") FilmStorage filmStorage, MPADao mpadao, GenreDao genreDao) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.mpaDao = mpadao;
        this.genreDao = genreDao;
    }

    public List<Film> findAll() {
        return filmStorage.findAllFilms();
    }

    public Film getFilmById(long filmId) {
        if (!filmStorage.isContains(filmId)) {
            throw new NotFoundException("Unable to find a movie with id " + filmId);
        }
        Film film = filmStorage.getFilmById(filmId);
        film.setGenres(filmStorage.getGenres(filmId));
        film.setMpa(mpaDao.findMPAById(film.getMpa().getId()));
        return film;
    }

    public Film create(Film film) {
        log.info("Create film: {}", film);
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new ValidationException("Incorrect name");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("The description can contain no more than 200 characters");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)) || film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Incorrect release date");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Duration cannot be negative");
        }

        long mpaId = film.getMpa().getId();
        if (!mpaDao.isContains(mpaId)) {
            log.warn("MPA with ID {} wasn't found", mpaId);
            throw new NotFoundException("MPA with ID " + mpaId + " wasn't found");
        }

        log.debug("MPA ID is valid: {}", mpaId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Long> existingGenreIds = genreDao.getAllGenreIds();

            for (Genre genre : film.getGenres()) {
                if (!existingGenreIds.contains(genre.getId())) {
                    log.warn("Genre with ID {} wasn't found", genre.getId());
                    throw new NotFoundException("Genre with ID " + genre.getId() + " wasn't found");
                }
            }
        }

        Film createFilm = filmStorage.createFilm(film);
        log.trace("The movie {} was added to the database", createFilm);
        return createFilm;
    }

    public Film update(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void addLikeFilms(long id, long userId) {
        if (userId > 0 && userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Не найден");
        }
        if (id > 0 && filmStorage.getFilmById(id) == null) {
            throw new NotFoundException("Не найден");
        }
        filmStorage.addLikeFilms(id, userId);
    }

    public void deleteLikeFilms(long id, long userId) {
        if (userId > 0 && userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Не найден");
        }
        if (id > 0 && filmStorage.getFilmById(id) == null) {
            throw new NotFoundException("Не найден");
        }
        filmStorage.deleteLikeFilms(id, userId);
    }

    public List<Film> findPopularFilms(int count) {
        log.debug("Execute findPopularFilms({})", count);
        return filmStorage.findPopularFilms(count);
    }

}
