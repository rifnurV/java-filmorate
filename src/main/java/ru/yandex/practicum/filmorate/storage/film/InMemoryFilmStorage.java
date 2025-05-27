package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private int nextId = 1;

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Optional<Film> getFilmById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            log.warn("Фильм {} с id {} уже был добавлен", film, film.getId());
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} добавлен", film);
        return film;
    }

    @Override
    public Film update(@Valid @RequestBody Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            log.warn("Не указан id для фильма {}", newFilm.getName());
            throw new NotFoundException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            films.put(newFilm.getId(), newFilm);
            log.info("Фильм {} обновлен", newFilm);
            return newFilm;
        } else {
            log.warn("Пост с id = {} не найден", newFilm.getId());
            throw new ValidationException("Пост с id = " + newFilm.getId() + " не найден");
        }
    }

    @Override
    public List<Film> findAll() {
        List<Film> filmsList = new ArrayList<>(films.values());
        return filmsList;
    }

    @Override
    public List<Film> findPopularFilms(int count) {
        return films.values()
                .stream()
                .sorted(Comparator.comparing(Film::getRate).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void addLikeFilms(long id, long userId) {
        films.get(id).addLikeFilms(userId);
    }

    @Override
    public void deleteLikeFilms(long id, long userId) {
        films.get(id).deleteLikeFilms(userId);
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        return nextId++;
    }
}
