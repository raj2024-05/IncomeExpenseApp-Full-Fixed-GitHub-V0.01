package com.example.incomeexpense

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LedgerActivity : AppCompatActivity() {
    private lateinit var dao: FinanceDao
    private var selectedAccount = ""
    private val df=SimpleDateFormat("dd-MM-yyyy",Locale.getDefault())
    override fun onCreate(b:Bundle?){
        super.onCreate(b); dao=AppDatabase.get(this).dao()
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;padding(20)}
        val title=TextView(this).apply{text="Account Ledger";textSize=24f}
        val account=Spinner(this)
        val table=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        val exportPdf=Button(this).apply{text="Export Ledger PDF"}
        val exportCsv=Button(this).apply{text="Export Ledger CSV"}
        root.addView(title);root.addView(account);root.addView(table)
        root.addView(exportPdf);root.addView(exportCsv);setContentView(root)
        lifecycleScope.launch{
            val accounts=dao.allAccounts()
            val names=accounts.map{it.name}.filter{it.isNotBlank()}
            account.adapter=ArrayAdapter(this@LedgerActivity,android.R.layout.simple_spinner_dropdown_item,names)
            if(names.isNotEmpty()){selectedAccount=names[0];load(selectedAccount,table)}
            account.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p:AdapterView<*>?)={}
                override fun onItemSelected(p:AdapterView<*>?,v:android.view.View?,pos:Int,id:Long){
                    if(pos<names.size){selectedAccount=names[pos];load(selectedAccount,table)}
                }
            }
        }
        exportPdf.setOnClickListener{exportLedgerPdf()}
        exportCsv.setOnClickListener{exportLedgerCsv()}
    }
    private fun load(ac:String,box:LinearLayout){
        lifecycleScope.launch{
            val inc=dao.incomeByAccount(ac);val exp=dao.expenseByAccount(ac)
            box.removeAllViews()
            val accountInfo=dao.allAccounts().firstOrNull{it.name==ac}
            var bal=accountInfo?.openingBalance ?: 0.0
            addRow(box,"Date","Particulars","Debit","Credit","Balance",true)
            val rows=(inc.map{LedgerRow(it.date,it.category,0.0,it.amount)}+
                    exp.map{LedgerRow(it.date,it.category,it.amount,0.0)}).sortedBy{it.date}
            addRow(box,"","","","Opening","%.2f".format(bal),false)
            rows.forEach{r->bal+=r.credit-r.debit;addRow(box,df.format(Date(r.date)),r.particular,"%.2f".format(r.debit),"%.2f".format(r.credit),"%.2f".format(bal),false)}
            addRow(box,"","","","Closing","%.2f".format(bal),true)
        }
    }
    private fun addRow(box:LinearLayout,a:String,b:String,c:String,d:String,e:String,bold:Boolean){
        val t=TextView(this).apply{text="$a   $b   $c   $d   $e";textSize=14f;padding=8}
        if(bold)t.setTypeface(null,Typeface.BOLD);box.addView(t)
    }
    private fun exportLedgerCsv(){
        lifecycleScope.launch{
            val inc=dao.incomeByAccount(selectedAccount);val exp=dao.expenseByAccount(selectedAccount)
            var bal=dao.allAccounts().firstOrNull{it.name==selectedAccount}?.openingBalance ?:0.0
            val rows=(inc.map{LedgerRow(it.date,it.category,0.0,it.amount)}+exp.map{LedgerRow(it.date,it.category,it.amount,0.0)}).sortedBy{it.date}
            val f=File(cacheDir,"ledger.csv");f.writeText("\uFEFFDate,Particulars,Debit,Credit,Balance\n")
            f.appendText(",, , ,$bal\n")
            rows.forEach{r->bal+=r.credit-r.debit;f.appendText("${df.format(Date(r.date))},${r.particular},${r.debit},${r.credit},$bal\n")}
            share(f,"text/csv")
        }
    }
    private fun exportLedgerPdf(){
        lifecycleScope.launch{
            val inc=dao.incomeByAccount(selectedAccount);val exp=dao.expenseByAccount(selectedAccount)
            var bal=dao.allAccounts().firstOrNull{it.name==selectedAccount}?.openingBalance ?:0.0
            val rows=(inc.map{LedgerRow(it.date,it.category,0.0,it.amount)}+exp.map{LedgerRow(it.date,it.category,it.amount,0.0)}).sortedBy{it.date}
            val pdf=PdfDocument();var pageNo=1;var page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,pageNo).create());var y=40
            fun line(x:String){if(y>800){pdf.finishPage(page);pageNo++;page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,pageNo).create());y=40};page.canvas.drawText(x,30f,y.toFloat(),Paint().apply{textSize=10f});y+=18}
            line("ACCOUNT LEDGER: $selectedAccount");line("Opening Balance: %.2f".format(bal));line("Date   Particulars   Debit   Credit   Balance")
            rows.forEach{r->bal+=r.credit-r.debit;line("${df.format(Date(r.date))}  ${r.particular.take(22)}  ${"%.2f".format(r.debit)}  ${"%.2f".format(r.credit)}  ${"%.2f".format(bal)}")}
            line("Closing Balance: %.2f".format(bal));pdf.finishPage(page)
            val f=File(cacheDir,"ledger.pdf");pdf.writeTo(f.outputStream());pdf.close();share(f,"application/pdf")
        }
    }
    private fun share(f:File,type:String){
        val uri=androidx.core.content.FileProvider.getUriForFile(this,"com.example.incomeexpense.fileprovider",f)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{this.type=type;putExtra(Intent.EXTRA_STREAM,uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)},"Share"))
    }
    data class LedgerRow(val date:Long,val particular:String,val debit:Double,val credit:Double)
}
