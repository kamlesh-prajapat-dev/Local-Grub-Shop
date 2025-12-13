package com.example.localgrubshop.di

import com.example.localgrubshop.data.repository.OrderRepositoryImpl
import com.example.localgrubshop.data.repository.ShopOwnerRepositoryImpl
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepository(impl: ShopOwnerRepositoryImpl): ShopOwnerRepository

//    @Binds
//    abstract fun bindDishesRepository(impl: DishesRepositoryImpl): DishesRepository

    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}