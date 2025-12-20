package com.example.localgrubshop.di

import com.example.localgrubshop.data.remote.firebase.repository.DishRepositoryImpl
import com.example.localgrubshop.data.remote.firebase.repository.OrderRepositoryImpl
import com.example.localgrubshop.data.remote.firebase.repository.ShopOwnerRepositoryImpl
import com.example.localgrubshop.data.remote.firebase.repository.UserRepositoryImpl
import com.example.localgrubshop.domain.repository.DishRepository
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindShopOwnerRepository(impl: ShopOwnerRepositoryImpl): ShopOwnerRepository

    @Binds
    abstract fun bindMenuRepository(impl: DishRepositoryImpl): DishRepository

    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
