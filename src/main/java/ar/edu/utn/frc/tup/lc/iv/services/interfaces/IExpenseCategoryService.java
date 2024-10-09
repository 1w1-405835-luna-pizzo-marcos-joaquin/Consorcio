package ar.edu.utn.frc.tup.lc.iv.services.interfaces;

import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import org.springframework.stereotype.Service;

@Service
public interface IExpenseCategoryService {
    ExpenseCategoryModel getCategoryModel(Integer id);
}
