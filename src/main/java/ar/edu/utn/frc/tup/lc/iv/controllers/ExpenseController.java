package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoRequestExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseExpense;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseService;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private IExpenseService expenseService;

    @PostMapping()
    public DtoResponseExpense postExpense(@RequestBody DtoRequestExpense request)
    {
        return expenseService.postExpense(request);
    }

}
