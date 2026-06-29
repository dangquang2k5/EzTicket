package huce.fit.myezticket.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    /** Lắng nghe realtime set các eventId đã yêu thích của user */
    fun getFavoriteIds(uid: String): Flow<Set<String>>

    /** Thêm sự kiện vào yêu thích */
    suspend fun addFavorite(uid: String, eventId: String)

    /** Xóa sự kiện khỏi yêu thích */
    suspend fun removeFavorite(uid: String, eventId: String)
}
