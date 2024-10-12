package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillExpenseInstallmentsRepository extends JpaRepository<BillExpenseInstallmentsEntity,Integer> {
    @Query(value = "SELECT bei.id, e.expense_type " +
            "FROM bills_record br " +
            "INNER JOIN bills_expense_owners beo ON br.id = beo.bill_record_id " +
            "INNER JOIN bills_expense_installments bei ON beo.id = bei.bill_expense_owner_id " +
            "INNER JOIN expense_installments ei ON bei.expense_installment_id = ei.id " +
            "INNER JOIN expenses e ON e.id = ei.expense_id " +
            "WHERE br.id = :billRecordId", nativeQuery = true)
    List<Object[]> findInstallmentIdAndExpenseTypeByBillRecordId(@Param("billRecordId") Integer billRecordId);
}
