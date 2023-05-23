package lootbox.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import lootbox.domain.Advertisement;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Seeder implements CommandLineRunner {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void run(String... args) throws Exception {
        Advertisement car = new Advertisement("bob@gmail.com", "0487667810", "car.jpeg", "my car", "car in pristine condition for 1M$");
        Advertisement cupboard = new Advertisement("carlos@gmail.com", "0477813710", "cupboard.jpeg", "my cupboard", "cupboard in pristine condition for 10$");

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        entityManager.persist(car);
        entityManager.persist(cupboard);

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
