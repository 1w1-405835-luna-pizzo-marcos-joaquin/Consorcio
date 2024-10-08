package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billexpenses")
public class BillExpenseController {
    @PostMapping("/generate")
    public ResponseEntity<BillExpenseDto> generateExpenses(@RequestBody(required = true)PeriodDto periodDto){
        BillExpenseDto result = new BillExpenseDto();
        return ResponseEntity.ok(result);
    }
}
