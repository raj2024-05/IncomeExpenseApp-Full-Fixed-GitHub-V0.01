package com.example.incomeexpense
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
class FinanceChartView(context:android.content.Context, private var income:Double=0.0, private var expense:Double=0.0):View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(c:Canvas){
        super.onDraw(c)
        val w=width.toFloat(); val h=height.toFloat()
        paint.textSize=38f
        c.drawText("Income vs Expenditure",20f,45f,paint)
        val max=maxOf(income,expense,1.0)
        paint.textSize=26f
        val base=h-50f
        val ih=(h-120f)*(income/max).toFloat()
        val eh=(h-120f)*(expense/max).toFloat()
        c.drawRect(60f,base-ih,180f,base,paint)
        c.drawRect(240f,base-eh,360f,base,paint)
        c.drawText("Income",55f,base+35f,paint)
        c.drawText("Expense",225f,base+35f,paint)
        paint.textSize=22f
        c.drawText("%.2f".format(income),55f,base-ih-12f,paint)
        c.drawText("%.2f".format(expense),235f,base-eh-12f,paint)
    }
    fun update(i:Double,e:Double){income=i;expense=e;invalidate()}
}
