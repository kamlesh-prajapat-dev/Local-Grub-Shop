package com.example.localgrubshop.di

import androidx.work.WorkManager
import com.example.localgrubshop.data.local.LocalDatabase
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.domain.repository.UserRepository
import com.example.localgrubshop.domain.usecase.DishUseCase
import com.example.localgrubshop.domain.usecase.OrderUseCase
import com.example.localgrubshop.domain.usecase.ShopOwnerUseCase
import com.example.localgrubshop.domain.usecase.UserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {

    @Provides
    @Singleton
    fun provideOrderUseCase(
        orderRepository: OrderRepository,
        notificationRepository: NotificationRepository,
        userRepository: UserRepository,
        workManager: WorkManager
    ): OrderUseCase {
        return OrderUseCase(
            orderRepository = orderRepository,
            notificationRepository = notificationRepository,
            userRepository = userRepository,
            workManager = workManager
        )
    }

    @Provides
    @Singleton
    fun provideShopOwnerUseCase(
        shopOwnerRepository: ShopOwnerRepository,
        localDatabase: LocalDatabase
    ): ShopOwnerUseCase {
        return ShopOwnerUseCase(
            shopOwnerRepository = shopOwnerRepository,
            localDatabase = localDatabase
        )
    }

    @Provides
    @Singleton
    fun provideDishUseCase(
        dishRepository: DishRepository
    ): DishUseCase{
        return DishUseCase(dishRepository)
    }

    @Provides
    @Singleton
    fun provideUserUseCase(
        userRepository: UserRepository
    ): UserUseCase {
        return UserUseCase(
            userRepository
        )
    }
}