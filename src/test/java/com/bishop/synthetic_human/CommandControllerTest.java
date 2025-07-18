package com.bishop.synthetic_human;


import com.bishop.synthetic_human.controller.CommandController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommandController.class)
class CommandControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void whenValidCommand_thenAccepted() throws Exception {
        String json = """
            {
              "description":"Тестовая команда",
              "priority":"COMMON",
              "author":"Tester",
              "time":"2025-07-18T16:00:00Z"
            }
        """;

        mvc.perform(post("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted());
    }

    @Test
    void whenInvalidPriority_thenBadRequest() throws Exception {
        String json = """
            {
              "description":"Тест",
              "priority":"WRONG",
              "author":"Tester",
              "time":"2025-07-18T16:00:00Z"
            }
        """;

        mvc.perform(post("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}

