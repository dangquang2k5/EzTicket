package huce.fit.myezticket.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import huce.fit.myezticket.data.model.Event
import huce.fit.myezticket.data.model.EventSchedule
import huce.fit.myezticket.data.model.TicketType
import java.util.Calendar

object FakeDataGenerator {
    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute)
        return Timestamp(calendar.time)
    }

    fun seedDataToFirebase() {
        val db = FirebaseFirestore.getInstance()
        val fakeEvents = listOf(
            Event(
                name = "Liveshow: Chân Trời Rực Rỡ",
                venueName = "Sân vận động Mỹ Đình",
                address = "Hà Nội",
                image_url = "https://images.unsplash.com/photo-1540039155732-6762b51cc2fb?q=80&w=800&auto=format&fit=crop",
                description = "<b>Giới thiệu sự kiện:</b><br>Trải nghiệm âm nhạc đỉnh cao với hệ thống âm thanh, ánh sáng đẳng cấp quốc tế.",
                category = "Âm nhạc",
                isBanner = true,
                isHot = true, // HIỂN THỊ CẢ LÊN BANNER LẪN MỤC ĐẶC BIỆT
                organizerName = "Live Nation Vietnam",
                organizerLogo = "https://ui-avatars.com/api/?name=LN&background=E8F5E9&color=4CAF50",
                // MẢNG MỚI NÀY NÈ:
                schedules = listOf(
                    EventSchedule(
                        date = createTimestamp(2026, 11, 20, 19, 30), // Lịch ngày 20
                        ticketTypes = listOf(
                            TicketType(name = "VVIP", price = 3500000L, originalPrice = 3500000L, quantity = 50),
                            TicketType(name = "VIP", price = 1500000L, originalPrice = 1500000L, quantity = 200),
                            TicketType(name = "Standard", price = 800000L, originalPrice = 800000L, quantity = 500)
                        )
                    ),
                    EventSchedule(
                        date = createTimestamp(2026, 11, 21, 19, 30), // Lịch ngày 21
                        ticketTypes = listOf(
                            TicketType(name = "VVIP", price = 3500000L, originalPrice = 3500000L, quantity = 0), // Đã hết vé
                            TicketType(name = "VIP", price = 1500000L, originalPrice = 1500000L, quantity = 150),
                            TicketType(name = "Standard", price = 800000L, originalPrice = 800000L, quantity = 400)
                        )
                    )
                )
            )
        )

        fakeEvents.forEach { event ->
            val docRef = db.collection("events").document()
            val eventWithId = event.copy(id = docRef.id)
            docRef.set(eventWithId)
                .addOnSuccessListener { Log.d("FakeData", "✅ Đã tạo: ${event.name}") }
                .addOnFailureListener { e -> Log.e("FakeData", "❌ Lỗi: ", e) }
        }
    }
}