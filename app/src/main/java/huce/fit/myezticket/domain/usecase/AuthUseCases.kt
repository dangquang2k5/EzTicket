package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUidUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): String = repository.getCurrentUserUid()
}

class GetCurrentUserEmailUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): String = repository.getCurrentUserEmail()
}

class GetCurrentUserDetailUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.getCurrentUserDetail()
}

class UpdateUserProfileUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(user: User) = repository.updateUserProfile(user)
}

class LogoutUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.logout()
}

class ChangePasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) = 
        repository.changePassword(currentPassword, newPassword)
}

class DeleteAccountUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.deleteAccount()
}

class UpdateNotificationSettingsUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(push: Boolean, booking: Boolean, promo: Boolean, system: Boolean) =
        repository.updateNotificationSettings(push, booking, promo, system)
}

class SendPasswordResetEmailUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) = repository.sendPasswordResetEmail(email)
}

class UploadAvatarUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(bytes: ByteArray) = repository.uploadAvatar(bytes)
}
