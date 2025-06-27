package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFilmStorageTest {

    private InMemoryFilmStorage inMemoryFilmStorage;
    private Film film1, film2, film3;

    @BeforeEach
    void setUp() {
        inMemoryFilmStorage = new InMemoryFilmStorage();

        film1 = new Film();
        film1.setName("Терминатор-1");
        film1.setDescription("А".repeat(200));
        film1.setReleaseDate(LocalDate.of(1984, 10, 26));
        film1.setDuration(60);

        film2 = new Film();
        film2.setName("Терминатор-2");
        film2.setDescription("В".repeat(200));
        film2.setReleaseDate(LocalDate.of(2010, 1, 1));
        film2.setDuration(90);

        film3 = new Film();
        film3.setName("Терминатор-3");
        film3.setDescription("В".repeat(200));
        film3.setReleaseDate(LocalDate.of(2020, 1, 1));
        film3.setDuration(150);
    }

    @Test
    void createFilmAndSave() {
        Film createdFilm = inMemoryFilmStorage.createFilm(film1);

        assertEquals(1, createdFilm.getId());
        assertEquals(film1.getName(), createdFilm.getName());
        assertEquals(film1.getDescription(), createdFilm.getDescription());
        assertEquals(film1.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film1.getDuration(), createdFilm.getDuration());
        assertNotNull(createdFilm.getId());
    }

    @Test
    void createFiles() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);
        Film createdFilm2 = inMemoryFilmStorage.createFilm(film2);

        assertEquals(1, createdFilm1.getId());
        assertEquals(2, createdFilm2.getId());
        assertEquals(2, inMemoryFilmStorage.findAllFilms().size());
    }

    @Test
    void updateFile() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);
        createdFilm1.setName("Зори здесь тихие");

        Film updatedFilm1 = inMemoryFilmStorage.updateFilm(createdFilm1);

        assertEquals(createdFilm1.getId(), updatedFilm1.getId());
        assertEquals(createdFilm1.getName(), updatedFilm1.getName());
        assertEquals(createdFilm1.getDescription(), updatedFilm1.getDescription());
        assertEquals(createdFilm1.getReleaseDate(), updatedFilm1.getReleaseDate());
        assertEquals(createdFilm1.getDuration(), updatedFilm1.getDuration());
    }

    @Test
    void updateFilmValidationException() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);

        createdFilm1.setId(2L);

        assertThrows(NullPointerException.class, () -> inMemoryFilmStorage.updateFilm(createdFilm1));
    }

    @Test
    void updateFilmNotFoundException() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);

        createdFilm1.setId(20L);

        assertThrows(NullPointerException.class, () -> inMemoryFilmStorage.updateFilm(createdFilm1));
    }

    @Test
    void findAll() {
        inMemoryFilmStorage.createFilm(film1);
        inMemoryFilmStorage.createFilm(film2);

        List<Film> films = inMemoryFilmStorage.findAllFilms();

        assertEquals(2, films.size());
    }

    @Test
    void findPopularFilms() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);
        Film createdFilm2 = inMemoryFilmStorage.createFilm(film2);

        inMemoryFilmStorage.addLikeFilms(createdFilm1.getId(), 1L);
        inMemoryFilmStorage.addLikeFilms(createdFilm1.getId(), 2L);
        inMemoryFilmStorage.addLikeFilms(createdFilm2.getId(), 3L);


        List<Film> popularFilms = inMemoryFilmStorage.findPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(createdFilm1.getId(), popularFilms.get(0).getId());
        assertEquals(createdFilm2.getId(), popularFilms.get(1).getId());
    }

    @Test
    void addLikeFilms() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);
        inMemoryFilmStorage.addLikeFilms(createdFilm1.getId(), 1L);
        inMemoryFilmStorage.addLikeFilms(createdFilm1.getId(), 2L);

        Set<Long> likes = createdFilm1.getUsersLike();

        assertTrue(likes.contains(1L));
        assertTrue(likes.contains(2L));
        assertEquals(2, likes.size());
    }

    @Test
    void deleteLikeFilms() {
        Film createdFilm1 = inMemoryFilmStorage.createFilm(film1);
        inMemoryFilmStorage.addLikeFilms(createdFilm1.getId(), 1L);
        inMemoryFilmStorage.deleteLikeFilms(createdFilm1.getId(), 1L);
        Set<Long> likes = createdFilm1.getUsersLike();

        assertEquals(0, likes.size());

    }
}