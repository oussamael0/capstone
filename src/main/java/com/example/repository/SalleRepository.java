package com.example.repository;

import com.example.model.Salle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalleRepository {
    List<Salle> findAvailableRooms(LocalDateTime dateDebut, LocalDateTime dateFin);
    List<Salle> searchRooms(Map<String, Object> criteres);
    List<Salle> getPaginatedRooms(int page, int pageSize);
    long countRooms();
}