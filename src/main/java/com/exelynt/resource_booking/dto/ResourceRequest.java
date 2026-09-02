package com.exelynt.resource_booking.dto;

import jakarta.validation.constraints.NotBlank;

public record ResourceRequest(

        @NotBlank(message = "Resource name is required")
        String name,

        String description,

        @NotBlank(message = "Resource type is required")
        String type,

        boolean available

) {
}