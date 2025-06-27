package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserStorageTest {

    private InMemoryUserStorage inMemoryUserStorage;
    private User user1, user2, user3, user4;

    @BeforeEach
    void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage();

        user1 = new User();
        user1.setEmail("user1@gmail.com");
        user1.setName("UserName1");
        user1.setLogin("UserLogin1");
        user1.setBirthday(LocalDate.of(1984, 2, 4));

        user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setName("UserName2");
        user2.setLogin("UserLogin2");
        user2.setBirthday(LocalDate.of(2000, 2, 4));

        user3 = new User();
        user3.setEmail("user3@gmail.com");
        user3.setName("UserName3");
        user3.setLogin("UserLogin3");
        user3.setBirthday(LocalDate.of(2001, 2, 4));

        user4 = new User();
        user4.setEmail("user4@gmail.com");
        user4.setName("UserName4");
        user4.setLogin("UserLogin4");
        user4.setBirthday(LocalDate.of(2002, 2, 4));
    }

    @Test
    void findById() {
        User userCreate = inMemoryUserStorage.create(user1);

        User findUser = inMemoryUserStorage.findById(userCreate.getId()).orElse(null);

        assertEquals(userCreate.getId(), findUser.getId());
        assertNotNull(findUser);
    }

    @Test
    void create() {
        User userCreate1 = inMemoryUserStorage.create(user1);
        User userCreate2 = inMemoryUserStorage.create(user2);

        assertEquals(2, inMemoryUserStorage.findAll().size());
    }

    @Test
    void update() {
        User userCreate = inMemoryUserStorage.create(user2);
        userCreate.setName("Tom");

        inMemoryUserStorage.update(userCreate);

        assertEquals("Tom", userCreate.getName());
    }

    @Test
    void updateNotFoundException() {
        User userCreate1 = inMemoryUserStorage.create(user1);
        userCreate1.setId(10L);

        assertThrows(NotFoundException.class, () -> inMemoryUserStorage.update(userCreate1));
    }

    @Test
    void findAll() {
        inMemoryUserStorage.create(user1);
        inMemoryUserStorage.create(user2);

        List<User> users = inMemoryUserStorage.findAll();

        assertEquals(2, users.size());
    }

}