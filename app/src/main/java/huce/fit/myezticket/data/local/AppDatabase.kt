package huce.fit.myezticket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import huce.fit.myezticket.data.local.dao.TicketDao
import huce.fit.myezticket.data.local.dao.EventDao
import huce.fit.myezticket.data.local.entity.TicketEntity
import huce.fit.myezticket.data.local.entity.EventEntity

@Database(entities = [TicketEntity::class, EventEntity::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun eventDao(): EventDao
}
