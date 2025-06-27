package ru.yandex.practicum.filmorate.storage.MPA;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mapper.MPAMapper;

import java.util.List;

@Slf4j
@Component
@Data
public class MPADaoImpl implements MPADao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public MPA findMPAById(long mpaId) {
        try {
            MPA mpa = jdbcTemplate.queryForObject(
                    "SELECT mpa_id, name FROM mpa_ratings WHERE mpa_id=?",
                    new MPAMapper(), mpaId);
            log.trace("The MPA rating {} was returned", mpa);
            return mpa;
        } catch (EmptyResultDataAccessException e) {
            log.error("MPA with id {} not found", mpaId);
            throw new NotFoundException("MPA with ID " + mpaId + " not found");
        }
    }

    @Override
    public List<MPA> findAll() {
        log.debug("findAll()");
        List<MPA> mpaList = jdbcTemplate.query(
                "SELECT mpa_id, name FROM mpa_ratings ORDER BY mpa_id",
                new MPAMapper());
        log.trace("These are all the mpa rating: {}", mpaList);
        return mpaList;
    }

    @Override
    public boolean isContains(long mpaId) {
        try {
            findMPAById(mpaId);
            log.trace("The MPA with id {} was found", mpaId);
            return true;
        } catch (NotFoundException e) {
            log.trace("The MPA with id {} was not found", mpaId);
            return false;
        }
    }
}
