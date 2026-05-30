package huce.fit.myezticket.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.EmailAuthProvider
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

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
            systemNotificationEnabled = doc.getBoolean("systemNotificationEnabled") ?: true
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

    override fun isBiometricEnabled(): Flow<Boolean> = flow {
        // Implementation with DataStore will go here
        emit(false)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        // Implementation with DataStore will go here
    }

    override fun logout() {
        auth.signOut()
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
            firestore.collection("users").document(uid).delete().await()
            firebaseUser.delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi khi xóa tài khoản. Thử đăng nhập lại trước khi xóa.")
        }
    }

    override suspend fun updateNotificationSettings(booking: Boolean, promo: Boolean, system: Boolean): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
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
}
