package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void add() throws Exception {
        Film film = validFilm();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.description").value("test"))
                .andExpect(jsonPath("$.releaseDate").value("2000-10-20"))
                .andExpect(jsonPath("$.duration").value(100));
    }

    @Test
    void addWithEmptyName() throws Exception {
        Film film = validFilm();
        film.setName("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Название фильма не может быть пустым"));
    }

    @Test
    void addWithEmptyDescription() throws Exception {
        Film film = validFilm();
        film.setDescription("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Описание не может быть пустым"));
    }

    @Test
    void addWithZeroDuration() throws Exception {
        Film film = validFilm();
        film.setDuration(0);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(0));
    }

    @Test
    void addWithInvalidReleaseDate() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    void findAll() throws Exception {
        createFilm(validFilm());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("test"));
    }

    @Test
    void findAllWithTwoFilms() throws Exception {
        Film first = validFilm();
        Film second = validFilm();
        second.setName("test2");
        second.setDescription("test2");
        second.setReleaseDate(LocalDate.of(2002, 10, 20));
        second.setDuration(1002);

        createFilm(first);
        createFilm(second);

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("test"))
                .andExpect(jsonPath("$[1].name").value("test2"));
    }

    @Test
    void update() throws Exception {
        createFilm(validFilm());
        Film updated = validFilm();
        updated.setId(1L);
        updated.setName("updated");
        updated.setDescription("updated");
        updated.setReleaseDate(LocalDate.of(2001, 1, 1));
        updated.setDuration(101);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("updated"))
                .andExpect(jsonPath("$.description").value("updated"))
                .andExpect(jsonPath("$.releaseDate").value("2001-01-01"))
                .andExpect(jsonPath("$.duration").value(101));
    }

    @Test
    void updateWithEmptyId() throws Exception {
        createFilm(validFilm());
        Film updated = validFilm();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id должен быть указан"));
    }

    @Test
    void updateWithEmptyName() throws Exception {
        createFilm(validFilm());
        Film updated = validFilm();
        updated.setId(1L);
        updated.setName("");

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"));
    }

    @Test
    void updateWithNotFoundId() throws Exception {
        Film updated = validFilm();
        updated.setId(999L);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с id = 999 не найден"));
    }

    private void createFilm(Film film) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("test");
        film.setDescription("test");
        film.setReleaseDate(LocalDate.of(2000, 10, 20));
        film.setDuration(100);
        return film;
    }
}
