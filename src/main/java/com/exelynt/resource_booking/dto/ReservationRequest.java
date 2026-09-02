package com.exelynt.resource_booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.exelynt.resource_booking.entity.ReservationStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(

        @NotNull(message = "Resource ID is required")
        Long resourceId,

        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        LocalDateTime endTime,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.01",
                message = "Price must be greater than zero"
        )
        BigDecimal price,

        ReservationStatus status

) {
}