package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void add() throws Exception {
        String requestBody = getContentFromFile("add/request/user.json");
        String responseBody = getContentFromFile("add/response/user.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void addWithEmptyEmail() throws Exception {
        String requestBody = getContentFromFile("add/request/email-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Электронная почта не может быть пустой"));
    }

    @Test
    void addWithEmptyLogin() throws Exception {
        String requestBody = getContentFromFile("add/request/login-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Логин не может быть пустой"));
    }

    @Test
    void addWithEmailWithoutAt() throws Exception {
        String requestBody = getContentFromFile("add/request/email-without-at.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Электронная почта должна содержать символ @"));
    }

    @Test
    void addWithLoginContainingSpace() throws Exception {
        String requestBody = getContentFromFile("add/request/login-with-space.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Логин не должен содержать пробел"));
    }

    @Test
    void addWithBirthdayInFuture() throws Exception {
        String requestBody = getContentFromFile("add/request/birthday-future.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Дата рождения не может быть в будущем"));
    }

    @Test
    void addWithEmptyNameSetsLoginAsName() throws Exception {
        String requestBody = getContentFromFile("add/request/name-empty-user.json");
        String responseBody = getContentFromFile("add/response/name-empty-user.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void findAll() throws Exception {
        createUser("add/request/user.json");
        String responseBody = getContentFromFile("findAll/response/user.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void findAllWithTwoUsers() throws Exception {
        createUser("add/request/user.json");
        createUser("add/request/user2.json");
        String responseBody = getContentFromFile("findAll/response/users.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void update() throws Exception {
        createUser("add/request/user.json");
        String requestBody = getContentFromFile("update/request/user.json");
        String responseBody = getContentFromFile("update/response/user.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void updateWithEmptyId() throws Exception {
        createUser("add/request/user.json");
        String requestBody = getContentFromFile("update/request/user-id-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Id должен быть указан"));
    }

    @Test
    void updateWithEmptyEmail() throws Exception {
        createUser("add/request/user.json");
        String requestBody = getContentFromFile("update/request/user-email-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Электронная почта не может быть пустой"));
    }

    @Test
    void updateWithNotFoundId() throws Exception {
        String requestBody = getContentFromFile("update/request/user-not-found.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Пользователь с id = 999 не найден"));
    }

    private void createUser(String fileName) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContentFromFile(fileName)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private String getContentFromFile(String fileName) {
        try {
            return Files.readString(ResourceUtils.getFile("classpath:" + fileName).toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException("Не открывается файл", exception);
        }
    }
}
