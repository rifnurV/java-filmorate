package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;

public interface FriendshipDao {
    void addFriend(long userId, long friendId, boolean isFriend);

    void deleteFriend(long userId, long friendId);

    List<Integer> getFriends(long userId);

    Friendship getFriend(long userId, long friendId);
}
