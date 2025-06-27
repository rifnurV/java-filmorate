package ru.yandex.practicum.filmorate.storage.MPA;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

public interface MPADao {

    MPA findMPAById(long id);

    List<MPA> findAll();

    boolean isContains(long id);
}
