package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User add(@RequestBody User user) {
        log.info("Создание нового пользователя: {}", user);
        userValidation(user);

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан с id = {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Обновление пользователя: {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            userValidation(newUser);

            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());

            log.info("Пользователь с id = {} обновлён", newUser.getId());
            return oldUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private void userValidation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации пользователя: Электронная почта не может быть пустой");
            throw new ConditionsNotMetException("Электронная почта не может быть пустой");
        } else if (!user.getEmail().contains("@")) {
            log.warn("Ошибка валидации пользователя: Электронная почта должна содержать символ @");
            throw new ConditionsNotMetException("Электронная почта должна содержать символ @");
        } else if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Ошибка валидации пользователя: Логин не может быть пустой");
            throw new ConditionsNotMetException("Логин не может быть пустой");
        } else if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации пользователя: Логин не должен содержать пробел");
            throw new ConditionsNotMetException("Логин не должен содержать пробел");
        } else if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации пользователя: Дата рождения не может быть в будущем");
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
