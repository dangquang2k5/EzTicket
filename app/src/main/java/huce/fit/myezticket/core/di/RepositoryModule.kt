package huce.fit.myezticket.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import huce.fit.myezticket.data.repository.AuthRepositoryImpl
import huce.fit.myezticket.data.repository.EventRepositoryImpl
import huce.fit.myezticket.data.repository.TicketRepositoryImpl
import huce.fit.myezticket.domain.repository.AuthRepository
import huce.fit.myezticket.domain.repository.EventRepository
import huce.fit.myezticket.domain.repository.TicketRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository

    @Binds
    abstract fun bindTicketRepository(
        ticketRepositoryImpl: TicketRepositoryImpl
    ): TicketRepository
}
