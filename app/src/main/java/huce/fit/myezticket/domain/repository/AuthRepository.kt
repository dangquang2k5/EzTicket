package huce.fit.myezticket.domain.repository

import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Check if phone exists and get User data (failed attempts, status)
    suspend fun getUserByPhone(phone: String): Resource<User>

    // Check if user exists by email
    suspend fun getUserByEmail(email: String): Resource<User>

    // Login with Email & Password
    suspend fun login(email: String, password: String): Resource<String>

    // Register a new user
    suspend fun register(user: User, password: String): Resource<User>

    // Update failed attempts
    suspend fun updateFailedAttempts(email: String, attempts: Int): Resource<Unit>

    // Lock account
    suspend fun lockAccount(email: String): Resource<Unit>

    // Check Biometric preference
    fun isBiometricEnabled(): Flow<Boolean>

    // Save Biometric preference
    suspend fun setBiometricEnabled(enabled: Boolean)

    // Logout
    fun logout()

    // Get current user details from Firestore
    suspend fun getCurrentUserDetail(): Resource<User>

    // Update user profile in Firestore
    suspend fun updateUserProfile(user: User): Resource<Unit>

    // Upload avatar to Firebase Storage and return URL
    suspend fun uploadAvatar(bytes: ByteArray): Resource<String>

    // Delete current account from Auth & Firestore
    suspend fun deleteAccount(): Resource<Unit>

    // Change password for current user
suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>

// Update only notification settings in Firestore
    suspend fun updateNotificationSettings(booking: Boolean, promo: Boolean, system: Boolean): Resource<Unit>
}
