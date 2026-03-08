package com.example.util;

import com.example.model.Salle;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PerformanceReport {

    private final EntityManagerFactory emf;
    private final Map<String, TestResult> results = new HashMap<>();

    public PerformanceReport(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void runPerformanceTests() {
        System.out.println("Exécution des tests de performance...");

        // Get Hibernate SessionFactory and Statistics
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // Test 1: Recherche de salles disponibles
        testPerformance("Recherche de salles disponibles", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                LocalDateTime start = LocalDateTime.now().plusDays(1);
                LocalDateTime end = start.plusHours(2);

                TypedQuery<Salle> query = em.createQuery(
                        "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN " +
                                "(SELECT r.salle.id FROM Reservation r " +
                                "WHERE (r.dateDebut <= :end AND r.dateFin >= :start))", Salle.class);
                query.setParameter("start", start);
                query.setParameter("end", end);
                return query.getResultList();
            } finally {
                em.close();
            }
        });

        // You can add other tests similarly...

        generateReport();
    }

    private void testPerformance(String testName, Supplier<?> testFunction) {
        System.out.println("Exécution du test: " + testName);

        long startTime = System.currentTimeMillis();
        Object result = testFunction.get();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();

        TestResult testResult = new TestResult();
        testResult.executionTime = executionTime;
        testResult.queryCount = stats.getQueryExecutionCount();
        testResult.entityLoadCount = stats.getEntityLoadCount();
        testResult.cacheHitCount = stats.getSecondLevelCacheHitCount();
        testResult.cacheMissCount = stats.getSecondLevelCacheMissCount();
        testResult.resultSize = (result instanceof java.util.Collection) ?
                ((java.util.Collection<?>) result).size() : (result != null ? 1 : 0);

        results.put(testName, testResult);
        System.out.println("Test terminé: " + testName + " en " + executionTime + "ms");

        stats.clear(); // reset stats for next test
    }

    private void generateReport() {
        String fileName = "performance_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("=== RAPPORT DE PERFORMANCE ===");
            writer.println("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=================================\n");

            for (Map.Entry<String, TestResult> entry : results.entrySet()) {
                writer.println("Test: " + entry.getKey());
                writer.println("Temps d'exécution: " + entry.getValue().executionTime + "ms");
                writer.println("Nombre de requêtes: " + entry.getValue().queryCount);
                writer.println("Entités chargées: " + entry.getValue().entityLoadCount);
                writer.println("Hits du cache: " + entry.getValue().cacheHitCount);
                writer.println("Miss du cache: " + entry.getValue().cacheMissCount);
                writer.println("Taille du résultat: " + entry.getValue().resultSize);
                writer.println("----------------------------------\n");
            }

            System.out.println("Rapport généré: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class TestResult {
        long executionTime;
        long queryCount;
        long entityLoadCount;
        long cacheHitCount;
        long cacheMissCount;
        int resultSize;
    }
}