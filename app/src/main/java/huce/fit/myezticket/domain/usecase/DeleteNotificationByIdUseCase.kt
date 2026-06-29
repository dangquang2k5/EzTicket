package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationByIdUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String) = repository.deleteNotificationById(notificationId)
}
