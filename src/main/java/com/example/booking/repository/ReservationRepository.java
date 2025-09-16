package com.example.booking.repository;

import com.example.booking.model.Reservation;
import com.example.booking.model.ReservationStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    @Query("select r from Reservation r where r.resource.id = :resourceId and r.status = 'CONFIRMED' and r.endTime > :start and r.startTime < :end")
    List<Reservation> findOverlappingConfirmed(@Param("resourceId") Long resourceId, @Param("start") Instant start, @Param("end") Instant end);
}
