package com.example.incomeexpense.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="income") data class IncomeEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val date:Long,val category:String,val account:String,val amount:Double,val paymentMode:String,val state:String,val description:String="",val referenceNumber:String="",val notes:String="",val createdAt:Long=System.currentTimeMillis(),val updatedAt:Long=System.currentTimeMillis())
@Entity(tableName="expense") data class ExpenseEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val date:Long,val category:String,val account:String,val amount:Double,val paymentMode:String,val state:String,val description:String="",val referenceNumber:String="",val notes:String="",val createdAt:Long=System.currentTimeMillis(),val updatedAt:Long=System.currentTimeMillis())
@Entity(tableName="categories") data class CategoryEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val type:String,val active:Boolean=true)
@Entity(tableName="accounts") data class AccountEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val type:String,val openingBalance:Double=0.0,val active:Boolean=true)
