package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> usersLike = new HashSet<>();
    private long rate;

    public void addLikeFilms(long userId) {
        usersLike.add(userId);
        rate = usersLike.size();
    }

    public void deleteLikeFilms(long userId) {
        usersLike.remove(userId);
        rate = usersLike.size();
    }
}
