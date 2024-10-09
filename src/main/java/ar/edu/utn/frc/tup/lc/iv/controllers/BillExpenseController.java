package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.services.impl.BillExpenseService;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IBillExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billexpenses")
public class BillExpenseController {
    private final IBillExpenseService billExpenseService;
    @Autowired
    public BillExpenseController(BillExpenseService billExpenseService) {
        this.billExpenseService = billExpenseService;
    }
    @PostMapping("/generate")
    public ResponseEntity<BillExpenseDto> generateExpenses(@RequestBody(required = true)PeriodDto periodDto){
        BillExpenseDto result = billExpenseService.generateBillExpense(periodDto);
        return ResponseEntity.ok(result);
    }
}
