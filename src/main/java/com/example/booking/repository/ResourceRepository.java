package com.example.booking.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.booking.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
