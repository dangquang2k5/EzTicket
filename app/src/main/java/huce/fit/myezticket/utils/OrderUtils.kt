package huce.fit.myezticket.utils

import huce.fit.myezticket.domain.model.PurchasedTicket
import java.util.Locale
import java.util.UUID

const val PAYMENT_TIMEOUT_MILLIS = 30 * 1000L

fun parseSelectedTicketsArg(selectedTicketsString: String?): Map<String, Int> {
    return selectedTicketsString
        ?.split(";")
        ?.mapNotNull { item ->
            val separatorIndex = item.lastIndexOf(":")
            if (separatorIndex <= 0) return@mapNotNull null

            val name = item.substring(0, separatorIndex)
            val quantity = item.substring(separatorIndex + 1).toIntOrNull() ?: 0
            name to quantity
        }
        ?.filter { (_, quantity) -> quantity > 0 }
        ?.toMap()
        ?: emptyMap()
}

fun serializeSelectedTicketsArg(selectedTickets: Map<String, Int>): String {
    return selectedTickets.entries.joinToString(";") { (name, quantity) -> "$name:$quantity" }
}

fun aggregateTicketQuantities(tickets: List<PurchasedTicket>): Map<String, Int> {
    return tickets
        .groupingBy { it.ticketTypeName }
        .fold(0) { quantity, ticket -> quantity + ticket.quantity }
}

fun createOrderCode(nowMillis: Long = System.currentTimeMillis()): String {
    val uniqueSuffix = UUID.randomUUID().toString().take(4).uppercase(Locale.ROOT)
    return "EZT${nowMillis.toString().takeLast(8)}$uniqueSuffix"
}

fun remainingPaymentSeconds(
    expiresAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): Int {
    return ((expiresAtMillis - nowMillis).coerceAtLeast(0L) + 999L).div(1000L).toInt()
}
