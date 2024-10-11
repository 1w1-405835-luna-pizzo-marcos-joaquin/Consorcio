package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoExpenseQuery;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface IExpenseService {
    DtoResponseExpense postExpense(DtoRequestExpense request,MultipartFile file);
    public DtoExpenseQuery getExpenseById(Integer expenseId);
    public List<DtoExpenseQuery> getExpenses(String expenseType, String category, String provider, String dateFrom, String dateTo);
}
