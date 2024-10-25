package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoCategory;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.DtoResponseDeleteExpense;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.ExpenseCategoryDTO;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IExpenseCategoryService {
    ExpenseCategoryDTO postCategory(String description);
    ExpenseCategoryModel getCategoryModel(Integer id);
    List<DtoCategory> getAllCategories();
    DtoResponseDeleteExpense deteleCategory(Integer id);
    ExpenseCategoryDTO putCategory(Integer id, String description);

}
