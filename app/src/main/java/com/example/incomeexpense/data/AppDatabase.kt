package com.example.incomeexpense.data
import android.content.Context
import androidx.room.*
@Database(entities=[IncomeEntity::class,ExpenseEntity::class,CategoryEntity::class,AccountEntity::class],version=1,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){abstract fun dao():FinanceDao
companion object{@Volatile private var I:AppDatabase?=null
fun get(c:Context)=I?:synchronized(this){I?:Room.databaseBuilder(c.applicationContext,AppDatabase::class.java,"income_expense.db").fallbackToDestructiveMigration().build().also{I=it}}}}
