package huce.fit.myezticket.domain.model

data class User(
    val uid: String,
    val phone: String,
    val email: String,
    val failedAttempts: Int = 0,
    val status: String = "ACTIVE",
    val role: String = "USER",
    val createdAt: Long = System.currentTimeMillis()
)
