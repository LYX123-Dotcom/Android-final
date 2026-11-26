package cn.campus.ledger.overview

import cn.campus.ledger.data.Bill

class MonthReport(bills: List<Bill>) {

    val bills: List<Bill> = bills
    val ranking: List<Map.Entry<String, Long>>
    val income: Long
    val expense: Long

    init {
        var incoming = 0L
        var outgoing = 0L
        val byCategory = HashMap<String, Long>()
        for (bill in bills) {
            if (bill.income) {
                incoming += bill.cents
            } else {
                outgoing += bill.cents
                byCategory[bill.category] = (byCategory[bill.category] ?: 0L) + bill.cents
            }
        }
        income = incoming
        expense = outgoing
        ranking = byCategory.entries.toList().sortedWith(
            compareByDescending<Map.Entry<String, Long>> { it.value }.thenBy { it.key }
        )
    }
}
