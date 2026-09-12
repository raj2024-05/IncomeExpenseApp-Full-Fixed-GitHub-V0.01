package com.example.incomeexpense.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface FinanceDao {
 @Insert suspend fun insertIncome(x:IncomeEntity):Long
 @Update suspend fun updateIncome(x:IncomeEntity)
 @Delete suspend fun deleteIncome(x:IncomeEntity)
 @Query("SELECT * FROM income ORDER BY date DESC,id DESC") fun incomes():Flow<List<IncomeEntity>>
 @Query("SELECT * FROM income WHERE date BETWEEN :start AND :end ORDER BY date ASC,id ASC") suspend fun incomeRange(start:Long,end:Long):List<IncomeEntity>
 @Query("SELECT COALESCE(SUM(amount),0) FROM income") fun totalIncome():Flow<Double>
 @Query("""SELECT * FROM income WHERE category LIKE '%' || :q || '%' OR account LIKE '%' || :q || '%' OR state LIKE '%' || :q || '%' OR paymentMode LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' ORDER BY date DESC,id DESC""") suspend fun searchIncome(q:String):List<IncomeEntity>
 @Query("""SELECT * FROM expense WHERE category LIKE '%' || :q || '%' OR account LIKE '%' || :q || '%' OR state LIKE '%' || :q || '%' OR paymentMode LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' ORDER BY date DESC,id DESC""") suspend fun searchExpense(q:String):List<ExpenseEntity>
 @Insert suspend fun insertExpense(x:ExpenseEntity):Long
 @Update suspend fun updateExpense(x:ExpenseEntity)
 @Delete suspend fun deleteExpense(x:ExpenseEntity)
 @Query("SELECT * FROM expense ORDER BY date DESC,id DESC") fun expenses():Flow<List<ExpenseEntity>>
 @Query("SELECT * FROM expense WHERE date BETWEEN :start AND :end ORDER BY date ASC,id ASC") suspend fun expenseRange(start:Long,end:Long):List<ExpenseEntity>
 @Query("SELECT COALESCE(SUM(amount),0) FROM expense") fun totalExpense():Flow<Double>
 @Query("SELECT * FROM income WHERE account=:account ORDER BY date ASC,id ASC") suspend fun incomeByAccount(account:String):List<IncomeEntity>
 @Query("SELECT * FROM expense WHERE account=:account ORDER BY date ASC,id ASC") suspend fun expenseByAccount(account:String):List<ExpenseEntity>
 @Query("SELECT * FROM income WHERE date BETWEEN :start AND :end ORDER BY date ASC,id ASC") suspend fun incomeRange(start:Long,end:Long):List<IncomeEntity>
 @Query("SELECT * FROM expense WHERE date BETWEEN :start AND :end ORDER BY date ASC,id ASC") suspend fun expenseRange(start:Long,end:Long):List<ExpenseEntity>

 @Insert suspend fun insertCategory(x:CategoryEntity):Long
 @Update suspend fun updateCategory(x:CategoryEntity)
 @Delete suspend fun deleteCategory(x:CategoryEntity)
 @Insert suspend fun insertAccount(x:AccountEntity):Long
 @Update suspend fun updateAccount(x:AccountEntity)
 @Delete suspend fun deleteAccount(x:AccountEntity)
 @Query("SELECT * FROM categories ORDER BY name") suspend fun categoryList():List<CategoryEntity>
 @Query("SELECT * FROM accounts ORDER BY name") suspend fun accountList():List<AccountEntity>
 @Insert suspend fun insertIncomeList(x:List<IncomeEntity>)
 @Insert suspend fun insertExpenseList(x:List<ExpenseEntity>)
 @Insert suspend fun insertCategoryList(x:List<CategoryEntity>)
 @Insert suspend fun insertAccountList(x:List<AccountEntity>)
 @Query("DELETE FROM income") suspend fun clearIncome()
 @Query("DELETE FROM expense") suspend fun clearExpense()
 @Query("DELETE FROM categories") suspend fun clearCategories()
 @Query("DELETE FROM accounts") suspend fun clearAccounts()
    @Query("SELECT * FROM accounts ORDER BY name")
    suspend fun allAccounts(): List<AccountEntity>

}