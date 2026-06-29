package huce.fit.myezticket.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import huce.fit.myezticket.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val applicationScope: CoroutineScope
) : AuthRepository {

    private fun pinCodeKey(uid: String) = stringPreferencesKey("pin_code_$uid")
    private fun pinFailedAttemptsKey(uid: String) = intPreferencesKey("pin_failed_attempts_$uid")
    private fun pinLockoutTimeKey(uid: String) = longPreferencesKey("pin_lockout_time_$uid")

    private fun mapDocumentToUser(doc: com.google.firebase.firestore.DocumentSnapshot): User {
        return User(
            uid = doc.id,
            phone = doc.getString("phone") ?: "",
            email = doc.getString("email") ?: "",
            failedAttempts = doc.getLong("failedAttempts")?.toInt() ?: 0,
            status = doc.getString("status") ?: "ACTIVE",
            role = doc.getString("role") ?: "USER",
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            avatarUrl = doc.getString("avatarUrl") ?: "",
            fullName = doc.getString("fullName") ?: "",
            birthDate = doc.getString("birthDate") ?: "",
            gender = doc.getString("gender") ?: "",
            bookingNotificationEnabled = doc.getBoolean("bookingNotificationEnabled") ?: true,
            promoNotificationEnabled = doc.getBoolean("promoNotificationEnabled") ?: true,
            systemNotificationEnabled = doc.getBoolean("systemNotificationEnabled") ?: true,
            pushNotificationEnabled = doc.getBoolean("pushNotificationEnabled") ?: true
        )
    }

    override suspend fun getUserByPhone(phone: String): Resource<User> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Resource.Error("Số điện thoại không tồn tại")
            } else {
                val doc = snapshot.documents.first()
                val user = mapDocumentToUser(doc)
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun getUserByEmail(email: String): Resource<User> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Resource.Error("Email không tồn tại")
            } else {
                val doc = snapshot.documents.first()
                val user = mapDocumentToUser(doc)
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun login(email: String, password: String): Resource<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Đăng nhập thất bại")
        }
    }

    override suspend fun register(user: User, password: String): Resource<User> {
        return try {
            // Kiểm tra trùng lặp email/phone ngay trước khi tạo Auth để hạn chế TOCTOU
            val phoneSnapshot = firestore.collection("users").whereEqualTo("phone", user.phone).get().await()
            if (!phoneSnapshot.isEmpty) {
                return Resource.Error("Số điện thoại đã được đăng ký")
            }
            val emailSnapshot = firestore.collection("users").whereEqualTo("email", user.email).get().await()
            if (!emailSnapshot.isEmpty) {
                return Resource.Error("Email đã được đăng ký")
            }

            // 1. Tạo tài khoản trên Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // 2. Cập nhật uid từ Firebase Auth vào model User
                val newUser = user.copy(uid = firebaseUser.uid)
                
                // 3. Lưu thông tin User vào Cloud Firestore
                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                
                Resource.Success(newUser)
            } else {
                Resource.Error("Không thể tạo tài khoản")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Đăng ký thất bại")
        }
    }

    override suspend fun updateFailedAttempts(email: String, attempts: Int): Resource<Unit> {
        return try {
            val snapshot = firestore.collection("users").whereEqualTo("email", email).get().await()
            if (!snapshot.isEmpty) {
                val docId = snapshot.documents.first().id
                firestore.collection("users").document(docId).update("failedAttempts", attempts).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update attempts")
        }
    }

    override suspend fun lockAccount(email: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection("users").whereEqualTo("email", email).get().await()
            if (!snapshot.isEmpty) {
                val docId = snapshot.documents.first().id
                firestore.collection("users").document(docId).update(
                    mapOf(
                        "failedAttempts" to 5,
                        "status" to "LOCKED"
                    )
                ).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to lock account")
        }
    }

    private fun biometricEnabledKey(uid: String) = booleanPreferencesKey("biometric_enabled_$uid")

    override fun isBiometricEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val uid = auth.currentUser?.uid.orEmpty()
            val key = biometricEnabledKey(uid)
            preferences[key] ?: false
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        val uid = auth.currentUser?.uid.orEmpty()
        val key = biometricEnabledKey(uid)
        dataStore.edit { preferences ->
            preferences[key] = enabled
        }
    }

    override fun logout() {
        val uid = auth.currentUser?.uid.orEmpty()
        applicationScope.launch {
            if (uid.isNotEmpty()) {
                val key = pinCodeKey(uid)
                val failedKey = pinFailedAttemptsKey(uid)
                val lockoutKey = pinLockoutTimeKey(uid)
                val biometricKey = biometricEnabledKey(uid)
                dataStore.edit { preferences ->
                    preferences.remove(key)
                    preferences.remove(failedKey)
                    preferences.remove(lockoutKey)
                    preferences.remove(biometricKey)
                }
            }
            // Sign out sau khi đã xóa DataStore
            auth.signOut()
        }
    }

    override fun getCurrentUserUid(): String {
        return auth.currentUser?.uid.orEmpty()
    }

    override fun getCurrentUserEmail(): String {
        return auth.currentUser?.email.orEmpty()
    }

    override suspend fun getCurrentUserDetail(): Resource<User> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                Resource.Success(mapDocumentToUser(doc))
            } else {
                val firebaseUser = auth.currentUser!!
                val user = User(
                    uid = uid,
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: ""
                )
                firestore.collection("users").document(uid).set(user).await()
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tải thông tin tài khoản")
        }
    }

    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            firestore.collection("users").document(uid).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi cập nhật thông tin cá nhân")
        }
    }

    override suspend fun uploadAvatar(bytes: ByteArray): Resource<String> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val ref = storage.reference.child("avatars/$uid.jpg")
            ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi tải ảnh đại diện lên")
        }
    }

    // Change password for current user
    override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> {
        val firebaseUser = auth.currentUser ?: return Resource.Error("User not logged in")
        return try {
            // Re-authenticate with current credentials
            val credential = EmailAuthProvider.getCredential(firebaseUser.email ?: "", currentPassword)
            firebaseUser.reauthenticate(credential).await()
            // Update password
            firebaseUser.updatePassword(newPassword).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to change password")
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        val firebaseUser = auth.currentUser ?: return Resource.Error("Chưa đăng nhập")
        val uid = firebaseUser.uid
        return try {
            // Xóa ảnh đại diện
            try {
                storage.reference.child("avatars/$uid.jpg").delete().await()
            } catch (e: Exception) {
                // Bỏ qua nếu không có ảnh
            }

            // Xóa subcollection favorites
            try {
                val favoritesSnapshot = firestore.collection("users").document(uid).collection("favorites").get().await()
                for (doc in favoritesSnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {}

            // Xóa notifications của user
            try {
                val notifSnapshot = firestore.collection("notifications").whereEqualTo("uid", uid).get().await()
                for (doc in notifSnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {}

            // Xóa vé đã mua
            try {
                val ticketsSnapshot = firestore.collection("purchasedTickets").whereEqualTo("uid", uid).get().await()
                for (doc in ticketsSnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {}

            // Xóa DataStore trước khi xóa user (khi uid vẫn còn khả dụng)
            if (uid.isNotEmpty()) {
                val key = pinCodeKey(uid)
                val failedKey = pinFailedAttemptsKey(uid)
                val lockoutKey = pinLockoutTimeKey(uid)
                val biometricKey = biometricEnabledKey(uid)
                dataStore.edit { preferences ->
                    preferences.remove(key)
                    preferences.remove(failedKey)
                    preferences.remove(lockoutKey)
                    preferences.remove(biometricKey)
                }
            }

            firestore.collection("users").document(uid).delete().await()
            firebaseUser.delete().await()
            auth.signOut()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi khi xóa tài khoản. Thử đăng nhập lại trước khi xóa.")
        }
    }

    override suspend fun updateNotificationSettings(push: Boolean, booking: Boolean, promo: Boolean, system: Boolean): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "pushNotificationEnabled" to push,
                    "bookingNotificationEnabled" to booking,
                    "promoNotificationEnabled" to promo,
                    "systemNotificationEnabled" to system
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi cập nhật cài đặt thông báo")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể gửi email đặt lại mật khẩu.")
        }
    }

    override fun getPinCode(): Flow<String> {
        return dataStore.data.map { preferences ->
            val uid = auth.currentUser?.uid.orEmpty()
            val key = pinCodeKey(uid)
            preferences[key] ?: ""
        }
    }

    override suspend fun savePinCode(pin: String) {
        val uid = auth.currentUser?.uid.orEmpty()
        val key = pinCodeKey(uid)
        dataStore.edit { preferences ->
            preferences[key] = huce.fit.myezticket.utils.SecurityUtils.hashSHA256(pin)
        }
    }

    override suspend fun clearPinCode() {
        val uid = auth.currentUser?.uid.orEmpty()
        val key = pinCodeKey(uid)
        val failedKey = pinFailedAttemptsKey(uid)
        val lockoutKey = pinLockoutTimeKey(uid)
        dataStore.edit { preferences ->
            preferences.remove(key)
            preferences.remove(failedKey)
            preferences.remove(lockoutKey)
        }
    }

    override fun getPinFailedAttempts(): Flow<Int> {
        return dataStore.data.map { preferences ->
            val uid = auth.currentUser?.uid.orEmpty()
            val key = pinFailedAttemptsKey(uid)
            preferences[key] ?: 0
        }
    }

    override suspend fun savePinFailedAttempts(attempts: Int) {
        val uid = auth.currentUser?.uid.orEmpty()
        val key = pinFailedAttemptsKey(uid)
        dataStore.edit { preferences ->
            preferences[key] = attempts
        }
    }

    override fun getPinLockoutTime(): Flow<Long> {
        return dataStore.data.map { preferences ->
            val uid = auth.currentUser?.uid.orEmpty()
            val key = pinLockoutTimeKey(uid)
            preferences[key] ?: 0L
        }
    }

    override suspend fun savePinLockoutTime(timeInMillis: Long) {
        val uid = auth.currentUser?.uid.orEmpty()
        val key = pinLockoutTimeKey(uid)
        dataStore.edit { preferences ->
            preferences[key] = timeInMillis
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Resource<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                // Check if user already exists in Firestore
                val userDoc = firestore.collection("users").document(uid).get().await()
                if (!userDoc.exists()) {
                    // Create new user in Firestore if not exist
                    val newUser = User(
                        uid = uid,
                        email = firebaseUser.email ?: "",
                        fullName = firebaseUser.displayName ?: "Google User",
                        avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                        status = "ACTIVE",
                        role = "USER",
                        createdAt = System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid).set(newUser).await()
                }
                Resource.Success(uid)
            } else {
                Resource.Error("Không thể xác thực với Firebase Auth")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Đăng nhập bằng Google thất bại")
        }
    }
}
