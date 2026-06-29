package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.AuthRepository
import javax.inject.Inject

class GetPinCodeUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.getPinCode()
}

class SavePinCodeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(pin: String) = repository.savePinCode(pin)
}

class ClearPinCodeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.clearPinCode()
}

class GetPinFailedAttemptsUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.getPinFailedAttempts()
}

class SavePinFailedAttemptsUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(attempts: Int) = repository.savePinFailedAttempts(attempts)
}

class GetPinLockoutTimeUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.getPinLockoutTime()
}

class SavePinLockoutTimeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(timeInMillis: Long) = repository.savePinLockoutTime(timeInMillis)
}
