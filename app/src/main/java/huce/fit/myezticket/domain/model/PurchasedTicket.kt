package huce.fit.myezticket.domain.model


import java.util.Locale

data class PurchasedTicket(
    val id: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val imageUrl: String = "",
    val orderCode: String = "",
    val ticketCode: String = "",
    val eventDateMillis: Long? = null,
    val location: String = "",
    val ticketTypeName: String = "",
    val quantity: Int = 1,
    val unitPrice: Long = 0,
    val totalPrice: Long = 0,
    val status: String = "Thành công",
    val customerPhone: String = "",
    val paymentMethod: String = "",
    val scheduleIndex: Int = 0,
    val createdAtMillis: Long? = null,
    val expiresAtMillis: Long? = null,
    val eventEndDateMillis: Long? = null
) {
    val isUpcoming: Boolean
        get() = isUpcomingAt(System.currentTimeMillis())

    fun isUpcomingAt(nowMillis: Long): Boolean {
        return (eventDateMillis ?: Long.MAX_VALUE) >= nowMillis
    }

    val qrPayload: String
        get() = ticketCode.ifBlank { id.ifBlank { orderCode } }

    fun splitIntoIndividualTickets(): List<PurchasedTicket> {
        val ticketCount = quantity.coerceAtLeast(1)
        return List(ticketCount) { index ->
            val individualTicketCode = when {
                ticketCount == 1 -> qrPayload
                ticketCode.isNotBlank() -> "$ticketCode-${index + 1}"
                id.isNotBlank() -> "$id-${index + 1}"
                else -> "$orderCode-${index + 1}"
            }
            copy(
                ticketCode = individualTicketCode,
                quantity = 1,
                totalPrice = unitPrice
            )
        }
    }

    val dayText: String
        get() = formatMillis(eventDateMillis, DAY_FORMAT)

    val monthText: String
        get() = eventDateMillis?.let { "Tháng ${formatMillis(it, MONTH_FORMAT)}" }.orEmpty()

    val yearText: String
        get() = formatMillis(eventDateMillis, YEAR_FORMAT)

    val timeText: String
        get() = eventDateMillis?.let { it ->
            val start = formatMillis(it, TIME_FORMAT)
            val end = eventEndDateMillis?.let { e -> formatMillis(e, TIME_FORMAT) }
            if (!end.isNullOrEmpty()) "$start - $end" else start
        } ?: ""

    val isPendingPayment: Boolean
        get() = status.trim() == STATUS_PENDING

    val isPaymentExpired: Boolean
        get() = isPendingPayment && (expiresAtMillis ?: 0L) <= System.currentTimeMillis()

    val displayStatus: String
        get() = if (isPaymentExpired) STATUS_CANCELLED else status.trim()

    companion object {
        const val STATUS_SUCCESS = "Thành công"
        const val STATUS_PENDING = "Đang chờ thanh toán"
        const val STATUS_CANCELLED = "Đã hủy"

        private val VI_LOCALE = Locale("vi", "VN")
        private val ZONE_ID = java.time.ZoneId.of("Asia/Ho_Chi_Minh")
        private val DAY_FORMAT = java.time.format.DateTimeFormatter.ofPattern("dd", VI_LOCALE)
        private val MONTH_FORMAT = java.time.format.DateTimeFormatter.ofPattern("MM", VI_LOCALE)
        private val YEAR_FORMAT = java.time.format.DateTimeFormatter.ofPattern("yyyy", VI_LOCALE)
        private val TIME_FORMAT = java.time.format.DateTimeFormatter.ofPattern("HH:mm", VI_LOCALE)

        private fun formatMillis(millis: Long?, formatter: java.time.format.DateTimeFormatter): String {
            if (millis == null) return ""
            val instant = java.time.Instant.ofEpochMilli(millis)
            val zdt = java.time.ZonedDateTime.ofInstant(instant, ZONE_ID)
            return zdt.format(formatter)
        }
    }
}
