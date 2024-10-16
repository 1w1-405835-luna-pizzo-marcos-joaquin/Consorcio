package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class ExpenseCategoryController {
    @Autowired
    private ExpenseCategoryService service;

    @DeleteMapping("/deleteById")
    public DtoResponseDeleteExpense deleteExpenseCategory(Integer id) {
        return service.deteleCategory(id);
    }
    @PutMapping("/putById")
    public ExpenseCategoryDTO PutExpenseCategory(Integer id, String description){
        return service.putCategory(id,description);
    }
}
