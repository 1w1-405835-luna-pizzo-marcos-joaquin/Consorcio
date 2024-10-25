package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class ExpenseCategoryController {
    @Autowired
    private IExpenseCategoryService expenseCategoryService;

    /**
     * Retrieves all available categories.
     *
     * @return a ResponseEntity containing a list of DtoCategory objects.
     */
    @GetMapping("/all")
    public ResponseEntity<List<DtoCategory>>  getAllCategories() {
        return ResponseEntity.ok( expenseCategoryService.getAllCategories());
    }
}
