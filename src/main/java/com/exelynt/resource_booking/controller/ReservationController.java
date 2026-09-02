package com.exelynt.resource_booking.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.exelynt.resource_booking.dto.ReservationRequest;
import com.exelynt.resource_booking.entity.Reservation;
import com.exelynt.resource_booking.entity.ReservationStatus;
import com.exelynt.resource_booking.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {

        this.reservationService = reservationService;
    }

    // =========================
    // CREATE
    // =========================

    @PostMapping
    public ResponseEntity<Reservation> create(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        Reservation reservation =
                reservationService.create(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservation);
    }

    // =========================
    // GET ALL / FILTER
    // =========================

    @GetMapping
    public ResponseEntity<Page<Reservation>> getReservations(
            Authentication authentication,

            @RequestParam(required = false)
            ReservationStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        boolean admin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        Page<Reservation> reservations =
                reservationService.getReservations(
                        authentication.getName(),
                        admin,
                        status,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                reservations
        );
    }

    // =========================
    // GET BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(
            @PathVariable Long id,
            Authentication authentication) {

        boolean admin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        Reservation reservation =
                reservationService.getById(
                        id,
                        authentication.getName(),
                        admin
                );

        return ResponseEntity.ok(
                reservation
        );
    }

    // =========================
    // UPDATE - ADMIN ONLY
    // =========================

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasRole('ADMIN')")
    public ResponseEntity<Reservation> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {

        return ResponseEntity.ok(
                reservationService.update(
                        id,
                        request
                )
        );
    }

    // =========================
    // DELETE - ADMIN ONLY
    // =========================

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}