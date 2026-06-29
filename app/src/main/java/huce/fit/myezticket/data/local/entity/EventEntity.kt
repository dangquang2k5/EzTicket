package huce.fit.myezticket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import huce.fit.myezticket.data.model.EventDto
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.EventSchedule

import huce.fit.myezticket.domain.model.TicketType

data class TicketTypeEntity(
    val name: String,
    val price: Long,
    val isVisible: Boolean,
    val quantity: Int
) {
    fun toDomainModel() = TicketType(name, price, isVisible, quantity)

    companion object {
        fun fromDomainModel(model: TicketType) = TicketTypeEntity(
            name = model.name,
            price = model.price,
            isVisible = model.isVisible,
            quantity = model.quantity
        )
    }
}

data class EventScheduleEntity(
    val dateMillis: Long?,
    val endDateMillis: Long?,
    val ticketTypes: List<TicketTypeEntity>
) {
    fun toDomainModel() = EventSchedule(dateMillis, endDateMillis, ticketTypes.map { it.toDomainModel() })

    companion object {
        fun fromDomainModel(model: EventSchedule) = EventScheduleEntity(
            dateMillis = model.dateMillis,
            endDateMillis = model.endDateMillis,
            ticketTypes = model.ticketTypes.map { TicketTypeEntity.fromDomainModel(it) }
        )
    }
}

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val venueName: String,
    val address: String,
    val imageUrl: String,
    val description: String,
    val category: String,
    val isBanner: Boolean,
    val isHot: Boolean,
    val isVisible: Boolean,
    val status: String,
    val organizerName: String,
    val organizerLogo: String,
    val schedules: List<EventScheduleEntity>
) {
    fun toDomainModel(): Event {
        return Event(
            id = id,
            name = name,
            venueName = venueName,
            address = address,
            image_url = imageUrl,
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

    companion object {
        fun fromDto(dto: EventDto): EventEntity {
            return EventEntity(
                id = dto.id,
                name = dto.name,
                venueName = dto.venueName,
                address = dto.address,
                imageUrl = dto.image_url,
                description = dto.description,
                category = dto.category,
                isBanner = dto.isBanner,
                isHot = dto.isHot,
                isVisible = dto.isVisible,
                status = dto.status,
                organizerName = dto.organizerName,
                organizerLogo = dto.organizerLogo,
                schedules = dto.schedules.map { EventScheduleEntity.fromDomainModel(it.toDomainModel()) }
            )
        }
    }
}
