package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.services.impl.ExpenseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class ExpenseCategoryController {
    @Autowired
    private ExpenseCategoryService service;

    @PostMapping("/postCategory")
    public ResponseEntity<ExpenseCategoryDTO> postCategory(String description){
        return ResponseEntity.ok(service.postCategory(description));
    }
    @DeleteMapping("/deleteById")
    public DtoResponseDeleteExpense deleteExpenseCategory(Integer id) {
        return service.deteleCategory(id);
    }
    @PutMapping("/putById")
    public ExpenseCategoryDTO PutExpenseCategory(Integer id, String description){
        return service.putCategory(id,description);
    }


    @GetMapping("/all")
    public ResponseEntity<List<DtoCategory>>  getAllCategories() {
        return ResponseEntity.ok( service.getAllCategories());
    }
}
