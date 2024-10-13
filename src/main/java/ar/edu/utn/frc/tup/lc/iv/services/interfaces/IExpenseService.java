package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoExpenseQuery;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.time.LocalDate;


@Service
public interface IExpenseService {
    public DtoExpenseQuery getExpenseById(Integer expenseId);
    public List<DtoExpenseQuery> getExpenses(String expenseType, String category, String provider, String dateFrom, String dateTo);
    ResponseEntity<DtoResponseExpense> postExpense(DtoRequestExpense request, MultipartFile file);

    DtoResponseDeleteExpense deteleExpense(Integer id);

    DtoResponseExpense createCreditNoteForExpense(Integer id);
    List<ExpenseModel> getExpenseByPaymentDateRange(LocalDate startDate, LocalDate endDate);
}
