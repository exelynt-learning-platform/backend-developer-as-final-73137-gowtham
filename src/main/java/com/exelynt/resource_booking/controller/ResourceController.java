package com.exelynt.resource_booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.exelynt.resource_booking.dto.ResourceRequest;
import com.exelynt.resource_booking.entity.Resource;
import com.exelynt.resource_booking.service.ResourceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(
            ResourceService resourceService) {

        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<List<Resource>> getAll() {

        return ResponseEntity.ok(
                resourceService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                resourceService.getById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> create(
            @Valid @RequestBody ResourceRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> update(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {

        return ResponseEntity.ok(
                resourceService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        resourceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}