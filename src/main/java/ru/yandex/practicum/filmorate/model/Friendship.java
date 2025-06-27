package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    private long id;
    private long userId;
    private long friendId;
    private boolean isFriend;
}
