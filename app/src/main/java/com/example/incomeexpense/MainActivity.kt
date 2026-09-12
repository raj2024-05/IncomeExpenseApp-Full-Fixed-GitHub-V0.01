package com.example.incomeexpense
import android.app.*
import android.content.*
import android.os.*
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.incomeexpense.data.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class MainActivity:AppCompatActivity(){
private lateinit var dao:FinanceDao
private val prefs by lazy{getSharedPreferences("settings",0)}
private lateinit var inc:TextView;private lateinit var exp:TextView;private lateinit var bal:TextView;private lateinit var entryList:LinearLayout
override fun onCreate(b:Bundle?){super.onCreate(b);applyTheme();setContentView(R.layout.activity_main);dao=AppDatabase.get(this).dao()
inc=findViewById(R.id.txtIncome);exp=findViewById(R.id.txtExpense);bal=findViewById(R.id.txtBalance);entryList=findViewById(R.id.entryList)
findViewById<MaterialButton>(R.id.btnAddIncome).setOnClickListener{entry(true)}
findViewById<MaterialButton>(R.id.btnAddExpense).setOnClickListener{entry(false)}
findViewById<MaterialButton>(R.id.btnSummary).setOnClickListener{summary()}
findViewById<MaterialButton>(R.id.btnBackup).setOnClickListener{backup()}
findViewById<MaterialButton>(R.id.btnRestore).setOnClickListener{pickRestore()}
findViewById<MaterialButton>(R.id.btnCsv).setOnClickListener{csv()}
findViewById<MaterialButton>(R.id.btnPdf).setOnClickListener{pdf()}
findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener{settings()}
findViewById<MaterialButton>(R.id.btnMasters).setOnClickListener{masters()}
findViewById<MaterialButton>(R.id.btnSummary).setOnClickListener{reports()}
findViewById<MaterialButton>(R.id.btnLedger).setOnClickListener{ledger()}
findViewById<MaterialButton>(R.id.btnDashboard).setOnClickListener{dashboard()}
findViewById<MaterialButton>(R.id.btnSearch).setOnClickListener{search()}
findViewById<MaterialButton>(R.id.btnProfessionalLedger).setOnClickListener{startActivity(Intent(this,LedgerActivity::class.java))}
findViewById<MaterialButton>(R.id.btnDateReport).setOnClickListener{customDateReport()}
lifecycleScope.launch{dao.incomes().collect{renderEntries()}}
lifecycleScope.launch{dao.expenses().collect{renderEntries()}}
lifecycleScope.launch{dao.totalIncome().collect{inc.text="Total Income: ${money(it)}";refresh()}}
lifecycleScope.launch{dao.totalExpense().collect{exp.text="Total Expenditure: ${money(it)}";refresh()}}}
private fun applyTheme(){AppCompatDelegate.setDefaultNightMode(when(prefs.getString("theme","system")){"light"->AppCompatDelegate.MODE_NIGHT_NO;"dark"->AppCompatDelegate.MODE_NIGHT_YES;else->AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM})}
private fun money(v:Double)="${prefs.getString("currency","₹")}%.2f".format(Locale.getDefault(),v)
private fun refresh(){lifecycleScope.launch{val i=dao.totalIncome().first();val e=dao.totalExpense().first();bal.text="Net Profit / (Loss): ${money(i-e)}"}}
private fun share(f:File,mime:String){val u=FileProvider.getUriForFile(this,"${packageName}.fileprovider",f);startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type=mime;putExtra(Intent.EXTRA_STREAM,u);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)},"Share"))}
private fun backup(){lifecycleScope.launch{val f=File(cacheDir,"backup_${System.currentTimeMillis()}.json");f.writeText(BackupRestore.make(this@MainActivity));share(f,"application/json")}}
private fun csv(){lifecycleScope.launch{share(ExportUtil.csv(this@MainActivity),"text/csv")}}
private fun pdf(){lifecycleScope.launch{share(ExportUtil.pdf(this@MainActivity,prefs.getString("currency","₹")!!),"application/pdf")}}
private fun pickRestore(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/json";addCategory(Intent.CATEGORY_OPENABLE)},20)}
override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==20&&c==RESULT_OK&&d?.data!=null)lifecycleScope.launch{try{contentResolver.openInputStream(d.data!!)?.bufferedReader()?.use{BackupRestore.restore(this@MainActivity,it.readText())};Toast.makeText(this@MainActivity,"Restore completed",Toast.LENGTH_LONG).show()}catch(e:Exception){Toast.makeText(this@MainActivity,"Restore failed: ${e.message}",Toast.LENGTH_LONG).show()}}}
private fun entry(isIncome:Boolean){val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(30,5,30,5)
fun edit(h:String)=EditText(this).apply{hint=h;inputType=InputType.TYPE_CLASS_TEXT;box.addView(this)}
val category=edit("Category");val account=edit("Account");val amount=edit("Amount");amount.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
val state=edit("State");val mode=edit("Payment Mode");val desc=edit("Description")
AlertDialog.Builder(this).setTitle(if(isIncome)"Add Income" else "Add Expenditure").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->lifecycleScope.launch{val a=amount.text.toString().toDoubleOrNull()?:0.0;if(a<=0){Toast.makeText(this@MainActivity,"Enter a valid amount",Toast.LENGTH_SHORT).show();return@launch};if(isIncome)dao.insertIncome(IncomeEntity(date=System.currentTimeMillis(),category=category.text.toString(),account=account.text.toString(),amount=a,paymentMode=mode.text.toString(),state=state.text.toString(),description=desc.text.toString())) else dao.insertExpense(ExpenseEntity(date=System.currentTimeMillis(),category=category.text.toString(),account=account.text.toString(),amount=a,paymentMode=mode.text.toString(),state=state.text.toString(),description=desc.text.toString()))}}.show()}
private fun summary(){lifecycleScope.launch{val i=dao.incomes().first();val e=dao.expenses().first();AlertDialog.Builder(this@MainActivity).setTitle("Financial Summary").setMessage("Income Entries: ${i.size}\\nExpenditure Entries: ${e.size}\\n\\nTotal Income: ${money(i.sumOf{it.amount})}\\nTotal Expenditure: ${money(e.sumOf{it.amount})}\\nNet Profit / (Loss): ${money(i.sumOf{it.amount}-e.sumOf{it.amount})}").setPositiveButton("Close",null).show()}}
private fun settings(){val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(30,5,30,5)
val currency=Spinner(this);currency.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("₹","$","€","£"));currency.setSelection(listOf("₹","$","€","£").indexOf(prefs.getString("currency","₹")));box.addView(TextView(this).apply{text="Currency"});box.addView(currency)
val theme=Spinner(this);theme.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("system","light","dark"));theme.setSelection(listOf("system","light","dark").indexOf(prefs.getString("theme","system")));box.addView(TextView(this).apply{text="Theme"});box.addView(theme)
val fy=EditText(this).apply{hint="Financial Year (e.g. 2026-27)";setText(prefs.getString("fy",""));box.addView(this)}
val fs=SeekBar(this).apply{max=60;progress=prefs.getInt("font",20);box.addView(TextView(this@MainActivity).apply{text="Font Size"});box.addView(this)}
AlertDialog.Builder(this).setTitle("Settings").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->prefs.edit().putString("currency",currency.selectedItem.toString()).putString("theme",theme.selectedItem.toString()).putString("fy",fy.text.toString()).putInt("font",fs.progress.coerceIn(10,50)).apply();applyTheme();recreate()}.show()}
private fun renderEntries(){lifecycleScope.launch{
 val ins=dao.incomes().first();val exs=dao.expenses().first();entryList.removeAllViews()
 val rows=mutableListOf<Pair<Boolean,Any>>();ins.forEach{rows.add(true to it)};exs.forEach{rows.add(false to it)}
 rows.sortedByDescending{if(it.first)(it.second as IncomeEntity).date else (it.second as ExpenseEntity).date}.take(100).forEach{r->
  val tv=TextView(this@MainActivity).apply{
   val d=if(r.first)(r.second as IncomeEntity).date else (r.second as ExpenseEntity).date
   val c=if(r.first)(r.second as IncomeEntity).category else (r.second as ExpenseEntity).category
   val a=if(r.first)(r.second as IncomeEntity).amount else (r.second as ExpenseEntity).amount
   text="${if(r.first)"INCOME" else "EXPENSE"}  ${java.text.SimpleDateFormat("dd-MM-yyyy",java.util.Locale.getDefault()).format(java.util.Date(d))}\n$c  ${money(a)}"
   textSize=16f;setPadding(12,12,12,12);setOnClickListener{editDelete(r.first,r.second)}
  };entryList.addView(tv)
 }
}}
private fun editDelete(isIncome:Boolean,obj:Any){AlertDialog.Builder(this).setTitle("Entry").setItems(arrayOf("Edit","Delete","Cancel")){_,w->when(w){0->Toast.makeText(this,"Use Add button to enter the corrected value in this version.",Toast.LENGTH_SHORT).show();1->lifecycleScope.launch{if(isIncome)dao.deleteIncome(obj as IncomeEntity)else dao.deleteExpense(obj as ExpenseEntity)}}}.show()}
private fun reports(){val o=arrayOf("Today","This Week","This Month","This Year","All Data");AlertDialog.Builder(this).setTitle("Reports").setItems(o){_,w->lifecycleScope.launch{
 val c=java.util.Calendar.getInstance();val end=c.timeInMillis
 when(w){0->{c.set(java.util.Calendar.HOUR_OF_DAY,0);c.set(java.util.Calendar.MINUTE,0);c.set(java.util.Calendar.SECOND,0);c.set(java.util.Calendar.MILLISECOND,0)};1->c.set(java.util.Calendar.DAY_OF_WEEK,c.firstDayOfWeek);2->c.set(java.util.Calendar.DAY_OF_MONTH,1);3->c.set(java.util.Calendar.DAY_OF_YEAR,1);4->{c.timeInMillis=Long.MIN_VALUE}}
 val st=c.timeInMillis;val i=if(w==4)dao.incomes().first()else dao.incomeRange(st,end);val e=if(w==4)dao.expenses().first()else dao.expenseRange(st,end)
 val ti=i.sumOf{it.amount};val te=e.sumOf{it.amount};val msg="Income: ${money(ti)}\nExpenditure: ${money(te)}\nNet Profit / (Loss): ${money(ti-te)}\n\nCategory Income:\n"+i.groupBy{it.category}.entries.joinToString("\n"){it.key+": "+money(it.value.sumOf{v->v.amount})}+"\n\nCategory Expenditure:\n"+e.groupBy{it.category}.entries.joinToString("\n"){it.key+": "+money(it.value.sumOf{v->v.amount})}+"\n\nState Income:\n"+i.groupBy{it.state}.entries.joinToString("\n"){it.key+": "+money(it.value.sumOf{v->v.amount})}+"\n\nState Expenditure:\n"+e.groupBy{it.state}.entries.joinToString("\n"){it.key+": "+money(it.value.sumOf{v->v.amount})}
 AlertDialog.Builder(this@MainActivity).setTitle("Report").setMessage(msg).setPositiveButton("Close",null).show()
}}.show()}

private fun masters(){
 val items=arrayOf("Accounts","Income Categories","Expense Categories")
 AlertDialog.Builder(this).setTitle("Master Data").setItems(items){_,which->
  when(which){0->accountMaster();1->categoryMaster("INCOME");2->categoryMaster("EXPENSE")}
 }.show()
}
private fun accountMaster(){lifecycleScope.launch{
 val a=dao.accountList();val names=if(a.isEmpty())"No accounts" else a.joinToString("\n"){it.name+"  | Opening: "+money(it.openingBalance)}
 AlertDialog.Builder(this@MainActivity).setTitle("Accounts").setMessage(names)
  .setPositiveButton("Add"){_,_->addAccount()}.setNegativeButton("Close",null).show()
}}
private fun addAccount(){val e=EditText(this);e.hint="Account name";AlertDialog.Builder(this).setTitle("Add Account").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->lifecycleScope.launch{if(e.text.toString().isNotBlank())dao.insertAccount(AccountEntity(name=e.text.toString(),type="GENERAL"))}}.show()}
private fun categoryMaster(type:String){lifecycleScope.launch{
 val c=dao.categoryList().filter{it.type==type};val names=if(c.isEmpty())"No categories" else c.joinToString("\n"){it.name}
 AlertDialog.Builder(this@MainActivity).setTitle("$type Categories").setMessage(names).setPositiveButton("Add"){_,_->addCategory(type)}.setNegativeButton("Close",null).show()
}}
private fun addCategory(type:String){val e=EditText(this);e.hint="Category name";AlertDialog.Builder(this).setTitle("Add Category").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->lifecycleScope.launch{if(e.text.toString().isNotBlank())dao.insertCategory(CategoryEntity(name=e.text.toString(),type=type))}}.show()}

private fun ledger(){lifecycleScope.launch{
 val accounts=dao.accountList().map{it.name}.filter{it.isNotBlank()}
 if(accounts.isEmpty()){Toast.makeText(this@MainActivity,"Add an account first",Toast.LENGTH_SHORT).show();return@launch}
 AlertDialog.Builder(this@MainActivity).setTitle("Select Account").setItems(accounts.toTypedArray()){_,which->
  lifecycleScope.launch{
   val name=accounts[which];val opening=dao.accountList().first{it.name==name}.openingBalance
   val i=dao.incomeByAccount(name);val e=dao.expenseByAccount(name)
   val rows=mutableListOf<Triple<Long,String,Double>>();i.forEach{rows.add(Triple(it.date,"Credit - ${it.category}",it.amount))};e.forEach{rows.add(Triple(it.date,"Debit - ${it.category}",it.amount))}
   rows.sortBy{it.first};var balance=opening
   val msg=buildString{append("Account: $name\nOpening Balance: ${money(opening)}\n\n")
    append("Date        Particulars                    Debit/Credit       Balance\n")
    rows.forEach{r->if(r.second.startsWith("Credit"))balance+=r.third else balance-=r.third;append("${df.format(Date(r.first))}  ${r.second}  ${money(r.third)}  ${money(balance)}\n")}
    append("\nClosing Balance: ${money(balance)}")}
   AlertDialog.Builder(this@MainActivity).setTitle("Ledger - $name").setMessage(msg).setPositiveButton("Close",null).show()
  }
 }.show()
}}

private fun dashboard(){lifecycleScope.launch{
 val i=dao.incomes().first().sumOf{it.amount};val e=dao.expenses().first().sumOf{it.amount}
 val box=LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(20,10,20,10)}
 val chart=FinanceChartView(this@MainActivity,i,e);box.addView(chart,LinearLayout.LayoutParams(-1,420))
 val t=TextView(this@MainActivity).apply{text="Income: ${money(i)}\nExpenditure: ${money(e)}\nNet Profit / (Loss): ${money(i-e)}\n\nIncome entries: ${dao.incomes().first().size}\nExpenditure entries: ${dao.expenses().first().size}";textSize=18f;setPadding(10,10,10,10)}
 box.addView(t)
 AlertDialog.Builder(this@MainActivity).setTitle("Dashboard").setView(box).setPositiveButton("Close",null).show()
}}

private fun search(){val e=EditText(this);e.hint="Category / Account / State / Payment Mode / Description"
 AlertDialog.Builder(this).setTitle("Search Entries").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Search"){_,_->lifecycleScope.launch{
  val q=e.text.toString().trim();if(q.isEmpty())return@launch
  val i=dao.searchIncome(q);val x=dao.searchExpense(q)
  val msg=buildString{append("Search: $q\n\n");append("INCOME\n");i.forEach{append("${df.format(Date(it.date))}  ${it.category}  ${money(it.amount)}\n")}
   append("\nEXPENDITURE\n");x.forEach{append("${df.format(Date(it.date))}  ${it.category}  ${money(it.amount)}\n")}
   append("\nResults: ${i.size+x.size}")}
  AlertDialog.Builder(this@MainActivity).setTitle("Search Results").setMessage(msg).setPositiveButton("Close",null).show()
 }}.show()}

private fun customDateReport(){
 val start=Calendar.getInstance();val end=Calendar.getInstance()
 fun pick(c:Calendar,title:String,done:(Long)->Unit){
  DatePickerDialog(this,{_,y,m,d->c.set(y,m,d,0,0,0);c.set(Calendar.MILLISECOND,0);done(c.timeInMillis)},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).setTitle(title).show()
 }
 pick(start,"Start Date"){sv->
  pick(end,"End Date"){ev->lifecycleScope.launch{
   val a=dao.incomeRange(sv,ev+86399999);val b=dao.expenseRange(sv,ev+86399999)
   val ti=a.sumOf{it.amount};val te=b.sumOf{it.amount}
   val msg="From ${df.format(Date(sv))} to ${df.format(Date(ev))}\n\nIncome: ${money(ti)}\nExpenditure: ${money(te)}\nNet Profit / (Loss): ${money(ti-te)}\n\nIncome by Account:\n"+
    a.groupBy{it.account}.entries.joinToString("\n"){it.key+": "+money(it.value.sumOf{v->v.amount})}+
    "\n\nExpenditure by Account:\n"+b.groupBy{it.account}.entries.joinToString("\n"){it.key+": "+money(it.value.sumOf{v->v.amount})}
   AlertDialog.Builder(this@MainActivity).setTitle("Custom Date Report").setMessage(msg).setPositiveButton("Close",null).show()
  }}
 }
}

}