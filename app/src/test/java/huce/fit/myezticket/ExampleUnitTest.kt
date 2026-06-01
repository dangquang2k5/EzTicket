package huce.fit.myezticket

import com.google.firebase.Timestamp
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.ui.screens.aggregateTicketQuantities
import huce.fit.myezticket.ui.screens.parseSelectedTicketsArg
import huce.fit.myezticket.ui.screens.remainingPaymentSeconds
import huce.fit.myezticket.ui.screens.serializeSelectedTicketsArg
import org.junit.Test

import org.junit.Assert.*
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun selectedTickets_roundTrip_preservesTicketNamesAndQuantity() {
        val selectedTickets = linkedMapOf(
            "Vé tiêu chuẩn" to 2,
            "VIP: Khu A" to 1
        )

        assertEquals(selectedTickets, parseSelectedTicketsArg(serializeSelectedTicketsArg(selectedTickets)))
    }

    @Test
    fun remainingPaymentSeconds_usesAbsoluteExpiryAndNeverReturnsNegativeValue() {
        assertEquals(2, remainingPaymentSeconds(expiresAtMillis = 2_001L, nowMillis = 1_000L))
        assertEquals(0, remainingPaymentSeconds(expiresAtMillis = 999L, nowMillis = 1_000L))
    }

    @Test
    fun purchasedTicket_splitIntoIndividualTickets_assignsUniqueQrPayloads() {
        val tickets = PurchasedTicket(
            id = "ticket-document",
            ticketCode = "ticket-document",
            quantity = 2,
            unitPrice = 100_000L,
            totalPrice = 200_000L
        ).splitIntoIndividualTickets()

        assertEquals(listOf("ticket-document-1", "ticket-document-2"), tickets.map { it.qrPayload })
        assertTrue(tickets.all { it.quantity == 1 })
        assertTrue(tickets.all { it.totalPrice == 100_000L })
    }

    @Test
    fun aggregateTicketQuantities_restoresPendingOrderQuantities() {
        val tickets = listOf(
            PurchasedTicket(ticketTypeName = "VIP", quantity = 1),
            PurchasedTicket(ticketTypeName = "VIP", quantity = 1),
            PurchasedTicket(ticketTypeName = "Standard", quantity = 1)
        )

        assertEquals(mapOf("VIP" to 2, "Standard" to 1), aggregateTicketQuantities(tickets))
    }

    @Test
    fun purchasedTicket_isUpcomingAt_changesWhenEventTimePasses() {
        val ticket = PurchasedTicket(eventDate = Timestamp(Date(2_000L)))

        assertTrue(ticket.isUpcomingAt(1_999L))
        assertFalse(ticket.isUpcomingAt(2_001L))
    }
}
