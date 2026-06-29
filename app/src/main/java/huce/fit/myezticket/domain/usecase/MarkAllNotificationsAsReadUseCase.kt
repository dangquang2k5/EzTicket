package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(uid: String) = repository.markAllAsRead(uid)
}
