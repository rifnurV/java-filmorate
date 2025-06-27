package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    Optional<User> findById(long id);

    User create(User user);

    User update(User user);

    List<User> findAll();

//    List<User> findAllFriendsCommon(long userId, long otherId);

//    void addFriend(long userId, long friendId);
//
//    void deleteFriends(long userId, long friendId);

    boolean userExists(long id);

    List<User> findAllFriends(long id);

    List<User> findAllFriendsCommon(long userId, long otherId);
}
