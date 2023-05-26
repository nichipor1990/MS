package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser(authorities = "ROLE_MODERATOR")
class RestExceptionHandlerTest extends BaseIntegrationTest {

    @Test
    void shouldHandleInvalidArgument() throws Exception {
        String testContent = """
                {
                  "username": "john_doe",
                  "email": "johndoeexample.com",
                  "password": "password123",
                  "firstName": "John",
                  "lastName": ""
                }
                """;
        mvc.perform(post("/api/users")
                        .content(testContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInternalServerErrorException() throws Exception {
        String testId = "615c3ab4-f966-11ed-be56-0242ac120005";
        mvc.perform(get("/api/users/" + testId))
                .andExpect(status().isInternalServerError());
    }


}