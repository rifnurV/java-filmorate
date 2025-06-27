package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.MPA.MPADao;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MPAService {
    private final MPADao mpaDao;

    public MPA getMpaById(Integer id) {
        if (id == null || !mpaDao.isContains(id)) {
            throw new NotFoundException("Negative or empty id was passed");
        }
        return mpaDao.findMPAById(id);
    }

    public Collection<MPA> getMpaList() {
        return mpaDao.findAll();
    }

}
