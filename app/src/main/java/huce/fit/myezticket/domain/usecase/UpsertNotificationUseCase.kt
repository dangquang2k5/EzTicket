package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.repository.NotificationRepository
import javax.inject.Inject

class UpsertNotificationUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(notification: AppNotification, docId: String) = repository.upsertNotification(notification, docId)
}
