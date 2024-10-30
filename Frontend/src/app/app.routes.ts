import { Routes } from '@angular/router';
import { ExpensesRegisterExpenseComponent } from './components/expenses-register-expense/expenses-register-expense.component';
import { ViewGastosAdminComponent } from './components/expenses-view-gastos-admin/view-gastos-admin.component';
import { ViewOwnerExpenseComponent } from './components/expenses-view-owner/view-owner-expense/view-owner-expense.component';
import { ExpensesViewCategoryComponent } from './components/expenses-view-category/expenses-view-category.component';
import { ReportExpenseComponent } from './components/expenses-report/report-expense/report-expense.component';

export const routes: Routes = [
  { path: 'viewExpenseAdmin', component: ViewGastosAdminComponent },
  { path: 'registerExpense/:id', component: ExpensesRegisterExpenseComponent },
  { path: 'registerExpense', component: ExpensesRegisterExpenseComponent },
  { path: 'viewCategory', component: ExpensesViewCategoryComponent },
  { path: 'viewExpenseOwner', component: ViewOwnerExpenseComponent },
  { path: 'estadisticas', component: ReportExpenseComponent }
];

export default routes;