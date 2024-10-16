package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity,Integer> {
    Optional<ExpenseCategoryEntity>findByDescription(String description);
}
