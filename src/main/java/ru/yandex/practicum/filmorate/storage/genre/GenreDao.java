package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreDao {
    Genre getGenreById(long id);

    List<Genre> getAllGenres();

    boolean isContains(int id);

    List<Long> getAllGenreIds();
}
