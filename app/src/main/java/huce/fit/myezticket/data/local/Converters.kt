package huce.fit.myezticket.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import huce.fit.myezticket.data.local.entity.EventScheduleEntity

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromScheduleList(value: List<EventScheduleEntity>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toScheduleList(value: String): List<EventScheduleEntity> {
        val type = object : TypeToken<List<EventScheduleEntity>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
