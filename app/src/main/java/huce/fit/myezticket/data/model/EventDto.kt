package huce.fit.myezticket.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.EventSchedule
import huce.fit.myezticket.domain.model.TicketType

data class EventDto(
    var id: String = "",
    val name: String = "",
    val venueName: String = "",
    val address: String = "",
    val image_url: String = "",
    val description: String = "",
    val category: String = "",
    
    @get:PropertyName("isBanner")
    @set:PropertyName("isBanner")
    var isBanner: Boolean = false,

    @get:PropertyName("isHot")
    @set:PropertyName("isHot")
    var isHot: Boolean = false,

    @get:PropertyName("isVisible")
    @set:PropertyName("isVisible")
    var isVisible: Boolean = true,
    val status: String = "AVAILABLE",
    val organizerName: String = "",
    val organizerLogo: String = "",
    val schedules: List<EventScheduleDto> = emptyList()
) {
    fun toDomainModel(): Event {
        return Event(
            id = id,
            name = name,
            venueName = venueName,
            address = address,
            image_url = image_url,
            description = description,
            category = category,
            isBanner = isBanner,
            isHot = isHot,
            isVisible = isVisible,
            status = status,
            organizerName = organizerName,
            organizerLogo = organizerLogo,
            schedules = schedules.map { it.toDomainModel() }
        )
    }
}

data class EventScheduleDto(
    val date: Timestamp? = null,
    val endDate: Timestamp? = null,
    val ticketTypes: List<TicketTypeDto> = emptyList()
) {
    fun toDomainModel(): EventSchedule {
        return EventSchedule(
            dateMillis = date?.toDate()?.time,
            endDateMillis = endDate?.toDate()?.time,
            ticketTypes = ticketTypes.map { it.toDomainModel() }
        )
    }

    companion object {
        fun fromDomainModel(model: EventSchedule): EventScheduleDto {
            return EventScheduleDto(
                date = model.dateMillis?.let { Timestamp(java.util.Date(it)) },
                endDate = model.endDateMillis?.let { Timestamp(java.util.Date(it)) },
                ticketTypes = model.ticketTypes.map { TicketTypeDto.fromDomainModel(it) }
            )
        }
    }
}

data class TicketTypeDto(
    val name: String = "",
    val price: Long = 0,
    @get:PropertyName("isVisible")
    @set:PropertyName("isVisible")
    var isVisible: Boolean = true,
    val quantity: Int = 0
) {
    fun toDomainModel(): TicketType {
        return TicketType(
            name = name,
            price = price,
            isVisible = isVisible,
            quantity = quantity
        )
    }

    companion object {
        fun fromDomainModel(model: TicketType): TicketTypeDto {
            return TicketTypeDto(
                name = model.name,
                price = model.price,
                isVisible = model.isVisible,
                quantity = model.quantity
            )
        }
    }
}
