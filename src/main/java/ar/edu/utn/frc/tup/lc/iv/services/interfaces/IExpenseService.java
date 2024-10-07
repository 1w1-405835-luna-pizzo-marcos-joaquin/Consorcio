package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import org.springframework.stereotype.Service;

@Service
public interface IExpenseService {
    DtoResponseExpense postExpense(DtoRequestExpense request);
}
