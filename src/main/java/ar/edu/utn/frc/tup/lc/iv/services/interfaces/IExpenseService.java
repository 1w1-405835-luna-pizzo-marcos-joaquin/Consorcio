package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public interface IExpenseService {
    ResponseEntity<DtoResponseExpense> postExpense(DtoRequestExpense request, MultipartFile file);
    List<ExpenseModel> getExpenseByPaymentDateRange(LocalDate from, LocalDate to);
}
