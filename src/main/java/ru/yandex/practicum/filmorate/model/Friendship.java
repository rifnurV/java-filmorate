package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    private Long id;
    private long userId;
    private Long friendId;
}
