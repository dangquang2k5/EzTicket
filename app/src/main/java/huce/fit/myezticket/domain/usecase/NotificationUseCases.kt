package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.NotificationRepository
import huce.fit.myezticket.domain.model.AppNotification
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(private val repository: NotificationRepository) {
    operator fun invoke(uid: String) = repository.getNotifications(uid)
}

class MarkNotificationAsReadUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String) = repository.markAsRead(notificationId)
}

class CreateNotificationUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(notification: AppNotification, docId: String) = repository.createNotificationIfNotExists(notification, docId)
}

class GetNotificationsSimpleUseCase @Inject constructor(private val repository: NotificationRepository) {
    operator fun invoke(uid: String) = repository.getNotificationsSimple(uid)
}

