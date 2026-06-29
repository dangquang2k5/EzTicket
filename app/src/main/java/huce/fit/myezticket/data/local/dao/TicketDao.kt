package huce.fit.myezticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import huce.fit.myezticket.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE uid = :uid ORDER BY createdAtMillis DESC")
    fun getTicketsFlow(uid: String): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Query("DELETE FROM tickets WHERE uid = :uid")
    suspend fun clearTickets(uid: String)

    @Transaction
    suspend fun replaceTickets(uid: String, tickets: List<TicketEntity>) {
        clearTickets(uid)
        insertTickets(tickets)
    }
}
