package com.example.booking.controller;

import com.example.booking.model.*;
import com.example.booking.repository.*;
import com.example.booking.spec.ReservationSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepo;
    private final ResourceRepository resourceRepo;
    private final UserRepository userRepo;

    public ReservationController(ReservationRepository reservationRepo, ResourceRepository resourceRepo, UserRepository userRepo) {
        this.reservationRepo = reservationRepo;
        this.resourceRepo = resourceRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public Page<Reservation> list(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication auth) {

        var parts = sort.split(",");
        var s = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
        PageRequest pr = PageRequest.of(page, size, s);

        Specification<Reservation> spec = Specification.where(ReservationSpecification.hasStatus(status))
                .and(ReservationSpecification.priceGte(minPrice))
                .and(ReservationSpecification.priceLte(maxPrice));

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            spec = spec.and(ReservationSpecification.ownedBy(auth.getName()));
        }
        return reservationRepo.findAll(spec, pr);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable Long id, Authentication auth) {
        Optional<Reservation> opt = reservationRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Reservation r = opt.get();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !r.getUser().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(r);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Reservation payload, Authentication auth) {
        
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        // validate 
        var res = resourceRepo.findById(payload.getResource().getId());
        if (res.isEmpty()) return ResponseEntity.badRequest().body("Resource not found");
        payload.setResource(res.get());
        payload.setUser(user);

        
        if (payload.getStatus() == ReservationStatus.CONFIRMED) {
            var overlaps = reservationRepo.findOverlappingConfirmed(payload.getResource().getId(), payload.getStartTime(), payload.getEndTime());
            if (!overlaps.isEmpty()) return ResponseEntity.status(409).body("Overlapping CONFIRMED reservation exists");
        }

        Reservation saved = reservationRepo.save(payload);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Reservation payload, Authentication auth) {
        Optional<Reservation> opt = reservationRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Reservation existing = opt.get();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !existing.getUser().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }

        // Allow updating status/price/times; resource change maybe restricted to admin
        existing.setPrice(payload.getPrice());
        existing.setStartTime(payload.getStartTime());
        existing.setEndTime(payload.getEndTime());
        if (isAdmin) {
            if (payload.getResource() != null && payload.getResource().getId() != null) {
                resourceRepo.findById(payload.getResource().getId()).ifPresent(existing::setResource);
            }
            if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
        } else {
            // user can only update to CANCELLED or maybe change times - (adjust according to policy)
            if (payload.getStatus() == ReservationStatus.CANCELLED) existing.setStatus(ReservationStatus.CANCELLED);
        }

        // overlapping check if status set to CONFIRMED
        if (existing.getStatus() == ReservationStatus.CONFIRMED) {
            var overlaps = reservationRepo.findOverlappingConfirmed(existing.getResource().getId(), existing.getStartTime(), existing.getEndTime());
            boolean conflict = overlaps.stream().anyMatch(r -> !r.getId().equals(existing.getId()));
            if (conflict) return ResponseEntity.status(409).body("Overlapping CONFIRMED reservation exists");
        }

        reservationRepo.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Reservation> opt = reservationRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Reservation existing = opt.get();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !existing.getUser().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        reservationRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
