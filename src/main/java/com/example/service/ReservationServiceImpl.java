package com.example.service;

import com.example.model.Reservation;
import com.example.repository.ReservationRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Optional;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
        this.reservationRepository = null;
    }

    // Constructeur utilisé par App.java
    public ReservationServiceImpl(EntityManager em, ReservationRepository reservationRepository) {
        this.emf = null;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservationRepository != null) {
            return reservationRepository.save(reservation);
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        if (reservationRepository != null) {
            return reservationRepository.findById(id);
        }
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Reservation.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Reservation reservation) {
        if (reservationRepository != null) {
            reservationRepository.update(reservation);
            return;
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(reservation);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Reservation reservation) {
        if (reservationRepository != null) {
            reservationRepository.delete(reservation);
            return;
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(reservation)) reservation = em.merge(reservation);
            em.remove(reservation);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}