package com.example.repository;

import com.example.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    void update(Reservation reservation);
    void delete(Reservation reservation);
    List<Reservation> findAll();
}