package com.example.railwayreservation.repository;
import com.example.railwayreservation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BookingRepository extends JpaRepository<Booking, Long> {}
