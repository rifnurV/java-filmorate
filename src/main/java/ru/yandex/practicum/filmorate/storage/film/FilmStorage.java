package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> getFilmById(long id);

    Film create(Film film);

    Film update(Film film);

    List<Film> findAll();

    List<Film> findPopularFilms(int count);

    void addLikeFilms(long id, long userId);

    void deleteLikeFilms(long id, long userId);
}
