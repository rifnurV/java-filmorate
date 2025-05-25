package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class After1895ReleaseDateValidator implements ConstraintValidator<After1895ReleaseDate, LocalDate> {
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext context) {
        return releaseDate != null && !releaseDate.isBefore(MIN_DATE) && !releaseDate.isAfter(LocalDate.now());
    }
}
