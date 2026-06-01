package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        phone: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading)

        // Validate Phone (10 digits)
        if (!phone.matches(Regex("^\\d{10}$"))) {
            emit(Resource.Error("Số điện thoại phải có đúng 10 chữ số"))
            return@flow
        }

        // Validate Email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emit(Resource.Error("Email không đúng định dạng"))
            return@flow
        }

        // Validate Password length
        if (password.length < 6) {
            emit(Resource.Error("Mật khẩu phải có ít nhất 6 ký tự"))
            return@flow
        }

        // Validate Password Match
        if (password != confirmPassword) {
            emit(Resource.Error("Mật khẩu xác nhận không trùng khớp"))
            return@flow
        }

        // Check if phone already exists
        val phoneCheck = authRepository.getUserByPhone(phone)
        if (phoneCheck is Resource.Success) {
            emit(Resource.Error("Số điện thoại đã được đăng ký"))
            return@flow
        }

        // Check if email already exists
        val emailCheck = authRepository.getUserByEmail(email)
        if (emailCheck is Resource.Success) {
            emit(Resource.Error("Email đã được đăng ký"))
            return@flow
        }

        // Proceed with Registration
        // Creating a new User with empty UID (Firebase will generate it)
        val newUser = User(
            uid = "",
            phone = phone,
            email = email,
            failedAttempts = 0,
            status = "ACTIVE",
            role = "USER",
            createdAt = System.currentTimeMillis()
        )

        when (val result = authRepository.register(newUser, password)) {
            is Resource.Success -> emit(Resource.Success(result.data))
            is Resource.Error -> emit(Resource.Error(result.message))
            is Resource.Loading -> {}
        }
    }
}
