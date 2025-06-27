package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void blankFilm() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(60);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
        assertEquals(true, violations.isEmpty());
    }

    @Test
    void validFilm() {
        Film film = new Film();
        film.setName("Терминатор-1");
        film.setDescription("Фантастика");
        film.setReleaseDate(LocalDate.of(1984, 10, 26));
        film.setDuration(60);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void longDescription() {
        Film film = new Film();
        film.setName("Терминатор-1");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(1984, 10, 26));
        film.setDuration(60);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
        assertEquals(true, violations.isEmpty());

    }

    @Test
    void negativeDuration() {
        Film film = new Film();
        film.setName("Терминатор-1");
        film.setDescription("Фантастика");
        film.setReleaseDate(LocalDate.of(1984, 10, 26));
        film.setDuration(-10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
        assertEquals(true, violations.isEmpty());
    }

    @Test
    void pastReleaseDate() {
        Film film = new Film();
        film.setName("Терминатор-1");
        film.setDescription("Фантастика");
        film.setReleaseDate(LocalDate.of(1889, 10, 26));
        film.setDuration(10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
        assertEquals(true, violations.isEmpty());

    }

}