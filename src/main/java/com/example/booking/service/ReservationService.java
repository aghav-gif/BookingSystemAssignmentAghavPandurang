package com.example.booking.service;

import com.example.booking.model.Reservation;
import com.example.booking.model.ReservationStatus;
import com.example.booking.model.User;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(ReservationRepository reservationRepository, 
                              UserRepository userRepository, 
                              ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    public Page<Reservation> list(ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRoles().contains(com.example.booking.model.Role.ROLE_ADMIN);

        if (!isAdmin) {
            return reservationRepository.findByUser(currentUser, pageable);
        }

        minPrice = minPrice != null ? minPrice : BigDecimal.ZERO;
        maxPrice = maxPrice != null ? maxPrice : BigDecimal.valueOf(Double.MAX_VALUE);

        if (status != null) {
            return reservationRepository.findByStatusAndPriceBetween(status, minPrice, maxPrice, pageable);
        }

        return reservationRepository.findAll(pageable);
    }

    public Reservation get(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRoles().contains(com.example.booking.model.Role.ROLE_ADMIN);

        if (!isAdmin && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied");
        }

        return reservation;
    }

    public Reservation create(Reservation reservation) {
        User currentUser = getCurrentUser();
        reservation.setUser(currentUser);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            boolean overlap = reservationRepository.findAll().stream()
                    .filter(r -> r.getResource().getId().equals(reservation.getResource().getId()))
                    .anyMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED &&
                            !(reservation.getEndTime().isBefore(r.getStartTime()) || reservation.getStartTime().isAfter(r.getEndTime()))
                    );
            if (overlap) throw new IllegalArgumentException("Overlapping confirmed reservation exists");
        }

        reservation.setCreatedAt(Instant.now());
        reservation.setUpdatedAt(Instant.now());
        return reservationRepository.save(reservation);
    }

    public Reservation update(Long id, Reservation updated) {
        Reservation reservation = get(id);
        reservation.setStatus(updated.getStatus());
        reservation.setPrice(updated.getPrice());
        reservation.setStartTime(updated.getStartTime());
        reservation.setEndTime(updated.getEndTime());
               return reservationRepository.save(reservation);
    }

    public void delete(Long id) {
        Reservation reservation = get(id);
        reservationRepository.delete(reservation);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
