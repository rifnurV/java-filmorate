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

public class UserValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validUser() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("Rifnur");
        user.setLogin("Rifnur");
        user.setBirthday(LocalDate.of(1984, 2, 4));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyMail() {
        User user = new User();
        user.setName("Rifnur");
        user.setLogin("Rifnur");
        user.setBirthday(LocalDate.of(1984, 2, 4));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void invalidMail() {
        User user = new User();
        user.setEmail("testgmail.com");
        user.setName("Rifnur");
        user.setLogin("Rifnur");
        user.setBirthday(LocalDate.of(1984, 2, 4));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Не корректная электронная почта", violations.iterator().next().getMessage());
    }

    @Test
    void invalidNullLogin() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("Rifnur");
        user.setBirthday(LocalDate.of(1984, 2, 4));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void invalidLogin() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("Rifnur");
        user.setLogin("Rif n ur");
        user.setBirthday(LocalDate.of(1984, 2, 4));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void invalidBirthday() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("Rifnur");
        user.setLogin("Rifnur");
        user.setBirthday(LocalDate.of(2025, 12, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }
}
