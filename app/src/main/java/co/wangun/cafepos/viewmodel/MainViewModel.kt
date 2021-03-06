package co.wangun.cafepos.viewmodel

import android.util.DisplayMetrics
import android.util.Log
import androidx.lifecycle.ViewModel
import co.wangun.cafepos.App
import co.wangun.cafepos.App.Companion.db
import co.wangun.cafepos.App.Companion.su
import co.wangun.cafepos.util.SessionUtils.Companion.TablesAmount_INT
import cowanguncafepos.Menu
import cowanguncafepos.Printer
import java.text.SimpleDateFormat
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class MainViewModel: ViewModel() {

    private val TAG: String by lazy { javaClass.simpleName }

    fun withCurrency(num: Double): String {
        return "£ %.2f".format(num)
    }

    fun invoiceInReceipt(tableInput: String, forReceipt: Boolean): String {
        val itemSplit = tableInput.split("?")
        val num = "#${itemSplit[0]}"
        val date = itemSplit[1].replace("-","")
        val time = itemSplit[2].replace(":","")
        val static = "Customer Number"
        val inv = "$num$date$time"

        val totalLen = 40
        val staticLen = static.length
        val invLen = inv.length
        val space = " ".repeat(totalLen - staticLen - invLen)

        return if(forReceipt) "$static$space$inv" else inv
    }

    fun tableInReceipt(tableInput: String): String {
        val itemSplit = tableInput.split("?")
        val num = itemSplit[0]
        val date = itemSplit[1]
        val time = itemSplit[2]

        val totalLength = 40
        val table = "Table $num"
        val dateTime = "$date $time"
        val space = " ".repeat(totalLength - table.length - dateTime.length)

        return "$table$space$dateTime"
    }

    fun totalInReceipt(tableInput: String): String {
        val itemSplit = tableInput.split("?")
        val num = itemSplit[0].toLong()
        val date = itemSplit[1]
        val time = itemSplit[2]
        val static = "Total"

        val totalPrice = db.orderQueries
                .selectAllTimeForTable(num, date, time)
                .executeAsList().map { it.price ?: 0.0 }.sum()
        val price = withCurrency(totalPrice)

        val totalLen = 27
        val staticLen = static.length
        val priceLen = price.length
        val space = " ".repeat(totalLen - staticLen - priceLen)
        return "$static$space$price"
    }

    fun itemInReceipt(nameInput: String, amountInput: Long, priceInput: Double): String {
        var name = nameInput
        val amount = "$amountInput  "
        val price = "    ${withCurrency(priceInput).drop(2)}"

        val totalLen = 40
        val nameLen = name.length
        val amountLen = amount.length
        val priceLen = price.length

        if (nameLen + amountLen + priceLen > totalLen) {
            val dif = totalLen - (nameLen + amountLen + priceLen)
            name = name.dropLast(dif)
        }

        val space = " ".repeat(totalLen - nameLen - amountLen - priceLen)
        return "${amount}${name}${space}${price}"
    }

    fun getPrinters(): List<Printer> {
        return db.printerQueries.selectAll().executeAsList().sortedBy { it.name }
    }

    fun getDetailOrders(tableInput: String): List<String> {
        val itemSplit = tableInput.split("?")
        val num = itemSplit[0].toLong()
        val date = itemSplit[1]
        val time = itemSplit[2]

        return db.orderQueries
                .selectAllTimeForTable(num, date, time)
                .executeAsList().map {
                    itemInReceipt(
                            it.name ?: "",
                            it.amount ?: 0,
                            it.price ?: 0.0
                    )
                }
    }
}