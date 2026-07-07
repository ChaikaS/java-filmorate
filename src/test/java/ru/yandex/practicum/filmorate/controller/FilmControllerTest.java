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
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void add() throws Exception {
        String requestBody = getContentFromFile("add/request/film.json");
        String responseBody = getContentFromFile("add/response/film.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void addWithEmptyName() throws Exception {
        String requestBody = getContentFromFile("add/request/name-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Название фильма не может быть пустым"));
    }

    @Test
    void addWithEmptyDescription() throws Exception {
        String requestBody = getContentFromFile("add/request/description-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Описание не может быть пустым"));
    }

    @Test
    void addWithZeroDuration() throws Exception {
        String requestBody = getContentFromFile("add/request/duration-zero.json");
        String responseBody = getContentFromFile("add/response/duration-zero.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void addWithInvalidReleaseDate() throws Exception {
        String requestBody = getContentFromFile("add/request/release-date-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    void findAll() throws Exception {
        createFilm("add/request/film.json");
        String responseBody = getContentFromFile("findAll/response/film.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/films"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void findAllWithTwoFilms() throws Exception {
        createFilm("add/request/film.json");
        createFilm("add/request/film2.json");
        String responseBody = getContentFromFile("findAll/response/films.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/films"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void update() throws Exception {
        createFilm("add/request/film.json");
        String requestBody = getContentFromFile("update/request/film.json");
        String responseBody = getContentFromFile("update/response/film.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(responseBody, false));
    }

    @Test
    void updateWithEmptyId() throws Exception {
        createFilm("add/request/film.json");
        String requestBody = getContentFromFile("update/request/id-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Id должен быть указан"));
    }

    @Test
    void updateWithEmptyName() throws Exception {
        createFilm("add/request/film.json");
        String requestBody = getContentFromFile("update/request/name-empty.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Название фильма не может быть пустым"));
    }

    @Test
    void updateWithNotFoundId() throws Exception {
        String requestBody = getContentFromFile("update/request/not-found.json");

        mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Фильм с id = 999 не найден"));
    }

    private void createFilm(String fileName) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
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
