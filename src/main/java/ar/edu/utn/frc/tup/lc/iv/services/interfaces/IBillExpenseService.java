package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;

public interface IBillExpenseService {
    BillExpenseDto generateBillExpense(PeriodDto periodDto);
}
