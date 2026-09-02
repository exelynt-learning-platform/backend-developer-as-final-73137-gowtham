package com.exelynt.resource_booking.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // ADMIN LOGIN
    // =========================

    private String getAdminToken() throws Exception {

        String requestBody = """
                {
                    "username": "admin",
                    "password": "Admin@123"
                }
                """;

        String response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }

    // =========================
    // USER LOGIN
    // =========================

    private String getUserToken() throws Exception {

        String requestBody = """
                {
                    "username": "user",
                    "password": "User@123"
                }
                """;

        String response = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }

    // =========================
    // ADMIN CAN CREATE RESOURCE
    // =========================

    @Test
    void adminCanCreateResource() throws Exception {

        String token = getAdminToken();

        String requestBody = """
                {
                    "name": "Test Conference Room",
                    "description": "Room for automated testing",
                    "type": "ROOM",
                    "available": true
                }
                """;

        mockMvc.perform(
                post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Test Conference Room"))
        .andExpect(jsonPath("$.type").value("ROOM"))
        .andExpect(jsonPath("$.available").value(true));
    }

    // =========================
    // USER CAN READ RESOURCES
    // =========================

    @Test
    void userCanReadResources() throws Exception {

        String token = getUserToken();

        mockMvc.perform(
                get("/resources")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // ADMIN CAN READ RESOURCES
    // =========================

    @Test
    void adminCanReadResources() throws Exception {

        String token = getAdminToken();

        mockMvc.perform(
                get("/resources")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // USER CANNOT CREATE RESOURCE
    // =========================

    @Test
    void userCannotCreateResource() throws Exception {

        String token = getUserToken();

        String requestBody = """
                {
                    "name": "Unauthorized Room",
                    "description": "User should not create this",
                    "type": "ROOM",
                    "available": true
                }
                """;

        mockMvc.perform(
                post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isForbidden());
    }

    // =========================
    // USER CANNOT DELETE RESOURCE
    // =========================

    @Test
    void userCannotDeleteResource() throws Exception {

        String adminToken = getAdminToken();

        String requestBody = """
                {
                    "name": "Protected Resource",
                    "description": "Testing delete authorization",
                    "type": "ROOM",
                    "available": true
                }
                """;

        String response = mockMvc.perform(
                post("/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        Long resourceId = json.get("id").asLong();

        String userToken = getUserToken();

        mockMvc.perform(
                delete("/resources/" + resourceId)
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isForbidden());
    }
}