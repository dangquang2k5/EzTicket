package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)

        // 1. Check if user exists and their status
        val userResource = authRepository.getUserByEmail(email)
        if (userResource is Resource.Error) {
            emit(Resource.Error("Email không tồn tại"))
            return@flow
        }

        if (userResource is Resource.Success) {
            val user = userResource.data
            if (user.status == "LOCKED") {
                emit(Resource.Error("Tài khoản đang tạm khóa"))
                return@flow
            }

            // 2. Attempt login
            when (val loginResult = authRepository.login(email, password)) {
                is Resource.Success -> {
                    // Reset failed attempts on success
                    if (user.failedAttempts > 0) {
                        authRepository.updateFailedAttempts(email, 0)
                    }
                    emit(Resource.Success(loginResult.data))
                }
                is Resource.Error -> {
                    // Handle failed attempt
                    val newAttempts = user.failedAttempts + 1
                    if (newAttempts >= 5) {
                        authRepository.lockAccount(email)
                        emit(Resource.Error("Tài khoản đang tạm khóa"))
                    } else {
                        authRepository.updateFailedAttempts(email, newAttempts)
                        // Lỗi mật khẩu không đúng
                        if (newAttempts >= 2) {
                            // Trả về Exception chứa số lần nhập sai để UI có thể xử lý Countdown
                            emit(Resource.Error("Mật khẩu không chính xác", Exception(newAttempts.toString())))
                        } else {
                            emit(Resource.Error("Mật khẩu không chính xác"))
                        }
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
