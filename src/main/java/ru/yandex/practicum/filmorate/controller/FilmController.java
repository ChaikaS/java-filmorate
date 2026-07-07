package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    static final LocalDate FIRST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film add(@RequestBody Film film) {
        log.info("Создание нового фильма: {}", film);
        filmValidation(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан с id = {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Обновление фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            filmValidation(newFilm);

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());

            log.info("Фильм с id = {} обновлён", newFilm.getId());
            return oldFilm;
        }
        log.warn("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private void filmValidation(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: Название фильма не может быть пустым");
            throw new ConditionsNotMetException("Название фильма не может быть пустым");
        } else if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.warn("Ошибка валидации фильма: Описание не может быть пустым");
            throw new ConditionsNotMetException("Описание не может быть пустым");
        } else if (film.getDescription().length() > 200) {
            log.warn("Ошибка валидации фильма: Максимальная длина описания — 200 символов");
            throw new ConditionsNotMetException("Максимальная длина описания — 200 символов");
        } else if (film.getDuration() < 0) {
            log.warn("Ошибка валидации фильма: Продолжительность фильма должна быть положительным числом");
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
        } else if (film.getReleaseDate().isBefore(FIRST_RELEASE_DATE)) {
            log.warn("Ошибка валидации фильма: Дата релиза — не раньше 28 декабря 1895 года");
            throw new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
