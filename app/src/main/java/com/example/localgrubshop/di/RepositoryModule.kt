package com.example.localgrubshop.di

import com.example.localgrubshop.data.remote.firebase.repository.MenuRepositoryImpl
import com.example.localgrubshop.data.remote.firebase.repository.OrderRepositoryImpl
import com.example.localgrubshop.data.remote.firebase.repository.ShopOwnerRepositoryImpl
import com.example.localgrubshop.domain.repository.MenuRepository
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

    @Binds
    abstract fun bindMenuRepository(impl: MenuRepositoryImpl): MenuRepository

    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}
