package ar.edu.utn.frc.tup.lc.iv.repositories;

import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillExpenseInstallmentsRepository extends JpaRepository<BillExpenseInstallmentsEntity,Integer> {
    @Query(value = "SELECT bei.id, e.expense_type, ec.description " +
            "FROM bills_record br " +
            "INNER JOIN bills_expense_owners beo ON br.id = beo.bill_record_id " +
            "INNER JOIN bills_expense_installments bei ON beo.id = bei.bill_expense_owner_id " +
            "INNER JOIN expense_installments ei ON bei.expense_installment_id = ei.id " +
            "INNER JOIN expenses e ON e.id = ei.expense_id " +
            "INNER JOIN expense_categories ec on e.expense_category_id = ec.id "+
            "WHERE br.id = :billRecordId", nativeQuery = true)
    List<Object[]> findInstallmentIdAndExpenseTypeByBillRecordId(@Param("billRecordId") Integer billRecordId);

    //TODO OJO ACA FILTRAR BILLRECORD ENABLED Y BILLEXPENSEINSTALLMENTS ENABLE
    @Query("SELECT bei FROM BillExpenseInstallmentsEntity bei " +
            "JOIN bei.expenseInstallment ei " +
            "WHERE ei.expense.id = :expenseId")
    Optional<List<BillExpenseInstallmentsEntity>> findByExpenseId(@Param("expenseId") Integer expenseId);
}
