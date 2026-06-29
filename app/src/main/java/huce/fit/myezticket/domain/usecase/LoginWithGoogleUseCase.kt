package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        if (idToken.isBlank()) {
            emit(Resource.Error("ID Token không hợp lệ."))
            return@flow
        }
        emit(authRepository.loginWithGoogle(idToken))
    }
}
