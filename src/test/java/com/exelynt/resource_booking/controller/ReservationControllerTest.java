package com.exelynt.resource_booking.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerTest {

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
    // CREATE RESOURCE FOR TEST
    // =========================

    private Long createResource() throws Exception {

        String adminToken = getAdminToken();

        String requestBody = """
                {
                    "name": "Reservation Test Room",
                    "description": "Room for reservation testing",
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

        return json.get("id").asLong();
    }

    // =========================
    // USER CAN CREATE RESERVATION
    // =========================

    @Test
    void userCanCreateReservation() throws Exception {

        String userToken = getUserToken();

        Long resourceId = createResource();

        String requestBody = """
                {
                    "resourceId": %d,
                    "startTime": "2030-09-05T10:00:00",
                    "endTime": "2030-09-05T12:00:00",
                    "price": 500.00,
                    "status": "PENDING"
                }
                """.formatted(resourceId);

        mockMvc.perform(
                post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isCreated());
    }

    // =========================
    // USER CAN VIEW RESERVATIONS
    // =========================

    @Test
    void userCanViewReservations() throws Exception {

        String userToken = getUserToken();

        mockMvc.perform(
                get("/reservations")
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // ADMIN CAN VIEW RESERVATIONS
    // =========================

    @Test
    void adminCanViewReservations() throws Exception {

        String adminToken = getAdminToken();

        mockMvc.perform(
                get("/reservations")
                        .header("Authorization", "Bearer " + adminToken)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // USER CAN FILTER BY STATUS
    // =========================

    @Test
    void userCanFilterReservationsByStatus() throws Exception {

        String userToken = getUserToken();

        mockMvc.perform(
                get("/reservations?status=PENDING")
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // USER CAN FILTER BY PRICE
    // =========================

    @Test
    void userCanFilterReservationsByPrice() throws Exception {

        String userToken = getUserToken();

        mockMvc.perform(
                get("/reservations?minPrice=100&maxPrice=1000")
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // USER CAN USE PAGINATION
    // =========================

    @Test
    void userCanUsePagination() throws Exception {

        String userToken = getUserToken();

        mockMvc.perform(
                get("/reservations?page=0&size=10")
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // USER CAN USE SORTING
    // =========================

    @Test
    void userCanUseSorting() throws Exception {

        String userToken = getUserToken();

        mockMvc.perform(
                get("/reservations?page=0&size=10&sortBy=price&direction=asc")
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isOk());
    }

    // =========================
    // USER CANNOT UPDATE RESERVATION
    // =========================

    @Test
    void userCannotUpdateReservation() throws Exception {

        String userToken = getUserToken();

        Long resourceId = createResource();

        String createRequest = """
                {
                    "resourceId": %d,
                    "startTime": "2030-10-05T10:00:00",
                    "endTime": "2030-10-05T12:00:00",
                    "price": 600.00,
                    "status": "PENDING"
                }
                """.formatted(resourceId);

        String response = mockMvc.perform(
                post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest)
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        Long reservationId = json.get("id").asLong();

        String updateRequest = """
                {
                    "resourceId": %d,
                    "startTime": "2030-10-05T11:00:00",
                    "endTime": "2030-10-05T13:00:00",
                    "price": 700.00,
                    "status": "CONFIRMED"
                }
                """.formatted(resourceId);

        mockMvc.perform(
                put("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest)
        )
        .andExpect(status().isForbidden());
    }

    // =========================
    // USER CANNOT DELETE RESERVATION
    // =========================

    @Test
    void userCannotDeleteReservation() throws Exception {

        String userToken = getUserToken();

        Long resourceId = createResource();

        String createRequest = """
                {
                    "resourceId": %d,
                    "startTime": "2030-11-05T10:00:00",
                    "endTime": "2030-11-05T12:00:00",
                    "price": 800.00,
                    "status": "PENDING"
                }
                """.formatted(resourceId);

        String response = mockMvc.perform(
                post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest)
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        Long reservationId = json.get("id").asLong();

        mockMvc.perform(
                delete("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + userToken)
        )
        .andExpect(status().isForbidden());
    }
}