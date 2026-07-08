package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void add() throws Exception {
        User user = validUser();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.login").value("testlogin"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    @Test
    void addWithEmptyEmail() throws Exception {
        User user = validUser();
        user.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта не может быть пустой"));
    }

    @Test
    void addWithEmptyLogin() throws Exception {
        User user = validUser();
        user.setLogin("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не может быть пустой"));
    }

    @Test
    void addWithEmailWithoutAt() throws Exception {
        User user = validUser();
        user.setEmail("invalid.email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта должна содержать символ @"));
    }

    @Test
    void addWithLoginContainingSpace() throws Exception {
        User user = validUser();
        user.setLogin("test login");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не должен содержать пробел"));
    }

    @Test
    void addWithBirthdayInFuture() throws Exception {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void addWithEmptyNameSetsLoginAsName() throws Exception {
        User user = validUser();
        user.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void findAll() throws Exception {
        createUser(validUser());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("test@mail.ru"));
    }

    @Test
    void findAllWithTwoUsers() throws Exception {
        User first = validUser();
        User second = validUser();
        second.setEmail("test2@mail.ru");
        second.setLogin("testlogin2");
        second.setName("Test User 2");
        second.setBirthday(LocalDate.of(1991, 2, 2));

        createUser(first);
        createUser(second);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("test@mail.ru"))
                .andExpect(jsonPath("$[1].email").value("test2@mail.ru"));
    }

    @Test
    void update() throws Exception {
        createUser(validUser());
        User updated = validUser();
        updated.setId(1L);
        updated.setEmail("updated@mail.ru");
        updated.setLogin("updatedlogin");
        updated.setName("Updated User");
        updated.setBirthday(LocalDate.of(1995, 5, 5));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("updated@mail.ru"))
                .andExpect(jsonPath("$.login").value("updatedlogin"))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.birthday").value("1995-05-05"));
    }

    @Test
    void updateWithEmptyId() throws Exception {
        createUser(validUser());
        User updated = validUser();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id должен быть указан"));
    }

    @Test
    void updateWithEmptyEmail() throws Exception {
        createUser(validUser());
        User updated = validUser();
        updated.setId(1L);
        updated.setEmail("");

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта не может быть пустой"));
    }

    @Test
    void updateWithNotFoundId() throws Exception {
        User updated = validUser();
        updated.setId(999L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь с id = 999 не найден"));
    }

    private void createUser(User user) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
