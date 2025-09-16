package com.example.booking.controller;

import com.example.booking.model.Resource;
import com.example.booking.repository.ResourceRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.*;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceRepository repo;

    public ResourceController(ResourceRepository repo) { this.repo = repo; }

    @GetMapping
    public Page<Resource> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(defaultValue = "id,asc") String sort) {
        var parts = sort.split(",");
        var s = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
        return repo.findAll(PageRequest.of(page, size, s));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Resource> create(@RequestBody Resource r) {
    	System.out.println("request comming");
        return ResponseEntity.ok(repo.save(r));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Resource> update(@PathVariable Long id, @RequestBody Resource r) {
        return repo.findById(id).map(existing -> {
            existing.setName(r.getName());
            existing.setType(r.getType());
            existing.setCapacity(r.getCapacity());
            existing.setDescription(r.getDescription());
            existing.setActive(r.isActive());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
