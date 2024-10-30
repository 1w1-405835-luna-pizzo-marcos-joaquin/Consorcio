package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseInstallmentEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public interface ExpenseInstallmentRepository extends JpaRepository<ExpenseInstallmentEntity,Integer> {
}
