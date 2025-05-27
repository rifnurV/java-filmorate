package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public FilmService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Optional<Film> getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public Film create(@Valid @RequestBody Film film) {
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new ValidationException("Не коррекное название");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание может содержать не более 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)) || film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Не корректная дата выпуска");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Длительность не может быть отрицательным");
        }
        return filmStorage.create(film);
    }

    public Film update(@Valid @RequestBody Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty() || film.getId() < 0) {
            throw new NotFoundException("Фильм не найден");
        }
        return filmStorage.update(film);
    }

    public void addLikeFilms(long id, long userId) {
        if (userId > 0 && userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Не найден");
        }
        if (id > 0 && filmStorage.getFilmById(id).isEmpty()) {
            throw new NotFoundException("Не найден");
        }
        filmStorage.addLikeFilms(id, userId);
    }

    public void deleteLikeFilms(long id, long userId) {
        if (userId > 0 && userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Не найден");
        }
        if (id > 0 && filmStorage.getFilmById(id).isEmpty()) {
            throw new NotFoundException("Не найден");
        }
        filmStorage.deleteLikeFilms(id, userId);
    }

    public List<Film> findPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Не может быть меньше 0");
        }
        return filmStorage.findPopularFilms(count);
    }
}
