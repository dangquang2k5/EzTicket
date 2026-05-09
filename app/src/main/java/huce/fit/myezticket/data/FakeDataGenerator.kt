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
                location = "Sân vận động Mỹ Đình, Hà Nội",
                image_url = "https://images.unsplash.com/photo-1540039155732-6762b51cc2fb?q=80&w=800&auto=format&fit=crop",
                description = "<b>Giới thiệu sự kiện:</b><br>Trải nghiệm âm nhạc đỉnh cao với hệ thống âm thanh, ánh sáng đẳng cấp quốc tế.",
                category = "Âm nhạc",
                isBanner = true,
                organizerName = "Live Nation Vietnam",
                organizerLogo = "https://ui-avatars.com/api/?name=LN&background=E8F5E9&color=4CAF50",
                // MẢNG MỚI NÀY NÈ:
                schedules = listOf(
                    EventSchedule(
                        date = createTimestamp(2026, 11, 20, 19, 30), // Lịch ngày 20
                        ticketTypes = listOf(
                            TicketType("VVIP", 3500000, 50),
                            TicketType("VIP", 1500000, 200),
                            TicketType("Standard", 800000, 500)
                        )
                    ),
                    EventSchedule(
                        date = createTimestamp(2026, 11, 21, 19, 30), // Lịch ngày 21
                        ticketTypes = listOf(
                            TicketType("VVIP", 3500000, 0), // Đã hết vé
                            TicketType("VIP", 1500000, 150),
                            TicketType("Standard", 800000, 400)
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