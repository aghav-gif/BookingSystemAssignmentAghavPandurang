package com.example.booking.spec;

import com.example.booking.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ReservationSpecification {
    public static Specification<com.example.booking.model.Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<com.example.booking.model.Reservation> priceGte(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<com.example.booking.model.Reservation> priceLte(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<com.example.booking.model.Reservation> ownedBy(String username) {
        return (root, query, cb) -> username == null ? null : cb.equal(root.get("user").get("username"), username);
    }
}
