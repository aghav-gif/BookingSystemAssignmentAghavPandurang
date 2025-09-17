package com.example.booking.service;

import com.example.booking.model.Resource;
import com.example.booking.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Page<Resource> list(Pageable pageable) {
        return resourceRepository.findAll(pageable);
    }

    public Resource get(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Resource not found"));
    }

    public Resource create(Resource resource) {
        return resourceRepository.save(resource);
    }

    public Resource update(Long id, Resource updated) {
        Resource resource = get(id);
        resource.setName(updated.getName());
        resource.setType(updated.getType());
        resource.setDescription(updated.getDescription());
        resource.setCapacity(updated.getCapacity());
        resource.setActive(updated.isActive());
        return resourceRepository.save(resource);
    }

    public void delete(Long id) {
        Resource resource = get(id);
        resourceRepository.delete(resource);
    }
}
