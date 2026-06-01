package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)

        // E01 - Email bỏ trống
        if (email.isBlank()) {
            emit(Resource.Error("Vui lòng nhập email."))
            return@flow
        }

        // E02 - Sai định dạng email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emit(Resource.Error("Email không hợp lệ."))
            return@flow
        }

        // E03 - Email không tồn tại
        val emailCheck = authRepository.getUserByEmail(email)
        if (emailCheck is Resource.Error) {
            emit(Resource.Error("Email chưa được đăng ký."))
            return@flow
        }

        // E04 - Lỗi gửi email
        when (val result = authRepository.sendPasswordResetEmail(email)) {
            is Resource.Success -> emit(Resource.Success(Unit))
            is Resource.Error -> emit(Resource.Error("Không thể gửi email đặt lại mật khẩu.\nVui lòng thử lại sau."))
            is Resource.Loading -> {}
        }
    }
}
