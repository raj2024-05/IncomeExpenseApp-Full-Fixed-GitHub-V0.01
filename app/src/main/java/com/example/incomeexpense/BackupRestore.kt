package com.example.incomeexpense
import android.content.Context
import com.example.incomeexpense.data.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
object BackupRestore {
private fun inc(o:JSONObject)=IncomeEntity(o.optLong("id"),o.optLong("date"),o.optString("category"),o.optString("account"),o.optDouble("amount"),o.optString("paymentMode"),o.optString("state"),o.optString("description"),o.optString("referenceNumber"),o.optString("notes"),o.optLong("createdAt"),o.optLong("updatedAt"))
private fun exp(o:JSONObject)=ExpenseEntity(o.optLong("id"),o.optLong("date"),o.optString("category"),o.optString("account"),o.optDouble("amount"),o.optString("paymentMode"),o.optString("state"),o.optString("description"),o.optString("referenceNumber"),o.optString("notes"),o.optLong("createdAt"),o.optLong("updatedAt"))
suspend fun make(c:Context):String{val d=AppDatabase.get(c).dao();val r=JSONObject().put("formatVersion",2).put("createdAt",System.currentTimeMillis());r.put("income",JSONArray().also{a->d.incomes().first().forEach{x->a.put(JSONObject().apply{put("id",x.id);put("date",x.date);put("category",x.category);put("account",x.account);put("amount",x.amount);put("paymentMode",x.paymentMode);put("state",x.state);put("description",x.description);put("referenceNumber",x.referenceNumber);put("notes",x.notes);put("createdAt",x.createdAt);put("updatedAt",x.updatedAt)})}});r.put("expense",JSONArray().also{a->d.expenses().first().forEach{x->a.put(JSONObject().apply{put("id",x.id);put("date",x.date);put("category",x.category);put("account",x.account);put("amount",x.amount);put("paymentMode",x.paymentMode);put("state",x.state);put("description",x.description);put("referenceNumber",x.referenceNumber);put("notes",x.notes);put("createdAt",x.createdAt);put("updatedAt",x.updatedAt)})}});return r.toString(2)}
suspend fun restore(c:Context,json:String){val r=JSONObject(json);val d=AppDatabase.get(c).dao();d.clearIncome();d.clearExpense();d.clearCategories();d.clearAccounts()
val ia=r.optJSONArray("income")?:JSONArray();val ea=r.optJSONArray("expense")?:JSONArray()
val il=mutableListOf<IncomeEntity>();val el=mutableListOf<ExpenseEntity>()
for(i in 0 until ia.length())il.add(inc(ia.getJSONObject(i)));for(i in 0 until ea.length())el.add(exp(ea.getJSONObject(i)))
if(il.isNotEmpty())d.insertIncomeList(il);if(el.isNotEmpty())d.insertExpenseList(el)}
}