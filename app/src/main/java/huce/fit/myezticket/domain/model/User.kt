package huce.fit.myezticket.domain.model

data class User(
    val uid: String = "",
    val phone: String = "",
    val email: String = "",
    val failedAttempts: Int = 0,
    val status: String = "ACTIVE",
    val role: String = "USER",
    val createdAt: Long = System.currentTimeMillis(),
    val avatarUrl: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val bookingNotificationEnabled: Boolean = true,
    val promoNotificationEnabled: Boolean = true,
    val systemNotificationEnabled: Boolean = true,
    val pushNotificationEnabled: Boolean = true
)
