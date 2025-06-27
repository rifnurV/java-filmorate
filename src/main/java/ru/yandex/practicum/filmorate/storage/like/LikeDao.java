package ru.yandex.practicum.filmorate.storage.like;

public interface LikeDao {
    void like(long filmId, long userId);

    void dislike(long filmId, long userId);

    int countLikes(long filmId);

    boolean isLiked(long filmId, long userId);

}
