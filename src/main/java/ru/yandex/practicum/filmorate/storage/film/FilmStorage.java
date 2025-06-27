package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film getFilmById(long id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    List<Film> findAllFilms();

    List<Film> findPopularFilms(int count);

    void addLikeFilms(long id, long userId);

    void deleteLikeFilms(long id, long userId);

    boolean isContains(long id);

    Set<Genre> getGenres(long filmId);
}
