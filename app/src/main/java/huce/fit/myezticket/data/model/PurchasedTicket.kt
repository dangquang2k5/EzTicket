package huce.fit.myezticket.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.text.SimpleDateFormat
import java.util.Locale

data class PurchasedTicket(
    var id: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val imageUrl: String = "",
    val orderCode: String = "",
    val eventDate: Timestamp? = null,
    val location: String = "",
    val ticketTypeName: String = "",
    val quantity: Int = 1,
    val unitPrice: Long = 0,
    val totalPrice: Long = 0,
    val status: String = "Thành công",
    val customerPhone: String = "",
    val paymentMethod: String = "",
    val scheduleIndex: Int = 0,
    val createdAt: Timestamp? = null
) {
    @get:Exclude
    val isUpcoming: Boolean
        get() = (eventDate?.toDate()?.time ?: Long.MAX_VALUE) >= System.currentTimeMillis()

    @get:Exclude
    val dayText: String
        get() = eventDate?.toDate()?.let { DAY_FORMAT.format(it) }.orEmpty()

    @get:Exclude
    val monthText: String
        get() = eventDate?.toDate()?.let { "Tháng ${MONTH_FORMAT.format(it)}" }.orEmpty()

    @get:Exclude
    val yearText: String
        get() = eventDate?.toDate()?.let { YEAR_FORMAT.format(it) }.orEmpty()

    @get:Exclude
    val timeText: String
        get() = eventDate?.toDate()?.let { TIME_FORMAT.format(it) }.orEmpty()

    companion object {
        private val VI_LOCALE = Locale("vi", "VN")
        private val DAY_FORMAT = SimpleDateFormat("dd", VI_LOCALE)
        private val MONTH_FORMAT = SimpleDateFormat("MM", VI_LOCALE)
        private val YEAR_FORMAT = SimpleDateFormat("yyyy", VI_LOCALE)
        private val TIME_FORMAT = SimpleDateFormat("HH:mm", VI_LOCALE)
    }
}
