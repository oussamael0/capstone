package com.example.repository;

import com.example.model.Salle;
import com.example.model.StatutReservation;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SalleRepositoryImpl implements SalleRepository {

    private final EntityManager em;

    public SalleRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime dateDebut, LocalDateTime dateFin) {
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT r.salle.id FROM Reservation r " +
                "WHERE r.statut <> :statutAnnule " +
                "AND r.dateDebut < :dateFin AND r.dateFin > :dateDebut)";

        TypedQuery<Salle> query = em.createQuery(jpql, Salle.class);
        query.setParameter("statutAnnule", StatutReservation.ANNULEE);
        query.setParameter("dateDebut", dateDebut);
        query.setParameter("dateFin", dateFin);

        return query.getResultList();
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteres) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT s FROM Salle s LEFT JOIN s.equipements e WHERE 1=1");

        if (criteres.containsKey("capaciteMin")) jpql.append(" AND s.capacite >= :capaciteMin");
        if (criteres.containsKey("capaciteMax")) jpql.append(" AND s.capacite <= :capaciteMax");
        if (criteres.containsKey("batiment"))    jpql.append(" AND s.batiment = :batiment");
        if (criteres.containsKey("etage"))       jpql.append(" AND s.etage = :etage");
        if (criteres.containsKey("equipement"))  jpql.append(" AND e.id = :equipement");

        TypedQuery<Salle> query = em.createQuery(jpql.toString(), Salle.class);

        if (criteres.containsKey("capaciteMin")) query.setParameter("capaciteMin", criteres.get("capaciteMin"));
        if (criteres.containsKey("capaciteMax")) query.setParameter("capaciteMax", criteres.get("capaciteMax"));
        if (criteres.containsKey("batiment"))    query.setParameter("batiment", criteres.get("batiment"));
        if (criteres.containsKey("etage"))       query.setParameter("etage", criteres.get("etage"));
        if (criteres.containsKey("equipement"))  query.setParameter("equipement", criteres.get("equipement"));

        return query.getResultList();
    }

    @Override
    public List<Salle> getPaginatedRooms(int page, int pageSize) {
        return em.createQuery("SELECT s FROM Salle s ORDER BY s.nom", Salle.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long countRooms() {
        return em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                .getSingleResult();
    }
}