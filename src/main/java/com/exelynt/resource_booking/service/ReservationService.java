package com.exelynt.resource_booking.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.exelynt.resource_booking.dto.ReservationRequest;
import com.exelynt.resource_booking.entity.Reservation;
import com.exelynt.resource_booking.entity.ReservationStatus;
import com.exelynt.resource_booking.entity.Resource;
import com.exelynt.resource_booking.entity.User;
import com.exelynt.resource_booking.repository.ReservationRepository;
import com.exelynt.resource_booking.repository.ResourceRepository;
import com.exelynt.resource_booking.repository.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // CREATE RESERVATION
    // =========================

    public Reservation create(
            ReservationRequest request,
            String username) {

        validateTimes(
                request.startTime(),
                request.endTime()
        );

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resource resource = resourceRepository
                .findById(request.resourceId())
                .orElseThrow(() ->
                        new RuntimeException("Resource not found"));

        if (!resource.isAvailable()) {
            throw new IllegalArgumentException(
                    "Resource is not available"
            );
        }

        Reservation reservation = new Reservation();

        // User comes from JWT/authentication
        reservation.setUser(user);

        reservation.setResource(resource);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setPrice(request.price());

        if (request.status() == null) {
            reservation.setStatus(
                    ReservationStatus.PENDING
            );
        } else {
            reservation.setStatus(
                    request.status()
            );
        }

        return reservationRepository.save(reservation);
    }

    // =========================
    // GET RESERVATIONS
    // =========================

    public Page<Reservation> getReservations(
            String username,
            boolean admin,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Size must be greater than zero"
            );
        }

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        // Allow only safe sortable fields
        if (!isAllowedSortField(sortBy)) {
            sortBy = "id";
        }

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Specification<Reservation> specification =
                Specification
                        .where(
                                ReservationSpecification
                                        .hasStatus(status)
                        )
                        .and(
                                ReservationSpecification
                                        .minPrice(minPrice)
                        )
                        .and(
                                ReservationSpecification
                                        .maxPrice(maxPrice)
                        );

        // USER can see only their own reservations
        if (!admin) {

            specification = specification.and(
                    ReservationSpecification
                            .belongsToUser(username)
            );
        }

        // ADMIN sees all reservations
        return reservationRepository.findAll(
                specification,
                pageable
        );
    }

    // =========================
    // GET RESERVATION BY ID
    // =========================

    public Reservation getById(
            Long id,
            String username,
            boolean admin) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found"
                                ));

        // ADMIN can access everything
        if (admin) {
            return reservation;
        }

        // USER can access only own reservation
        if (!reservation.getUser()
                .getUsername()
                .equals(username)) {

            throw new AccessDeniedException(
                    "You can access only your own reservation"
            );
        }

        return reservation;
    }

    // =========================
    // UPDATE RESERVATION
    // =========================

    public Reservation update(
            Long id,
            ReservationRequest request) {

        validateTimes(
                request.startTime(),
                request.endTime()
        );

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found"
                                ));

        Resource resource =
                resourceRepository.findById(
                        request.resourceId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Resource not found"
                        ));

        if (!resource.isAvailable()) {
            throw new IllegalArgumentException(
                    "Resource is not available"
            );
        }

        reservation.setResource(resource);
        reservation.setStartTime(
                request.startTime()
        );
        reservation.setEndTime(
                request.endTime()
        );
        reservation.setPrice(
                request.price()
        );

        if (request.status() != null) {
            reservation.setStatus(
                    request.status()
            );
        }

        return reservationRepository.save(
                reservation
        );
    }

    // =========================
    // DELETE RESERVATION
    // =========================

    public void delete(Long id) {

        if (!reservationRepository.existsById(id)) {

            throw new RuntimeException(
                    "Reservation not found"
            );
        }

        reservationRepository.deleteById(id);
    }

    // =========================
    // VALIDATION
    // =========================

    private void validateTimes(
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {

        if (!endTime.isAfter(startTime)) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }
    }

    private boolean isAllowedSortField(
            String sortBy) {

        return sortBy.equals("id")
                || sortBy.equals("price")
                || sortBy.equals("startTime")
                || sortBy.equals("endTime")
                || sortBy.equals("status");
    }
}