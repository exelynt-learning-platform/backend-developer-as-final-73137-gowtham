package com.exelynt.resource_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exelynt.resource_booking.dto.ResourceRequest;
import com.exelynt.resource_booking.entity.Resource;
import com.exelynt.resource_booking.repository.ResourceRepository;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(
            ResourceRepository resourceRepository) {

        this.resourceRepository = resourceRepository;
    }

    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    public Resource getById(Long id) {

        return resourceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Resource not found"
                        ));
    }

    public Resource create(ResourceRequest request) {

        Resource resource = new Resource(
                request.name(),
                request.description(),
                request.type(),
                request.available()
        );

        return resourceRepository.save(resource);
    }

    public Resource update(
            Long id,
            ResourceRequest request) {

        Resource resource = getById(id);

        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setType(request.type());
        resource.setAvailable(request.available());

        return resourceRepository.save(resource);
    }

    public void delete(Long id) {

        Resource resource = getById(id);

        resourceRepository.delete(resource);
    }
}