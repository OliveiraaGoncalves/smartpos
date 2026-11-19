package com.raimundo.smartpos.di

import com.raimundo.smartpos.BuildConfig
import com.raimundo.smartpos.core.domain.gateway.PaymentGateway
import com.raimundo.smartpos.core.network.HttpPaymentGateway
import com.raimundo.smartpos.core.networkmock.MockBackend
import com.raimundo.smartpos.core.networkmock.MockBackendInterceptor
import com.raimundo.smartpos.core.security.AesGcmPayloadCipher
import com.raimundo.smartpos.core.security.KeyProvider
import com.raimundo.smartpos.core.security.PayloadCipher
import com.raimundo.smartpos.core.security.StaticKeyProvider
import com.raimundo.smartpos.core.security.hexToBytes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Flavor `mock`: o backend roda dentro do próprio processo (MockBackendInterceptor do
 * `core:network-mock`). O cliente HTTP, a serialização e a criptografia são exatamente os
 * que falariam com o backend real — só o interceptor muda (ver README).
 */
@Module
@InstallIn(SingletonComponent::class)
object MockNetworkModule {
    @Provides
    @Singleton
    fun provideKeyProvider(): KeyProvider =
        StaticKeyProvider(BuildConfig.PAYLOAD_KEY_ID, BuildConfig.DEV_PAYLOAD_KEY_HEX.hexToBytes())

    @Provides
    @Singleton
    fun providePayloadCipher(keyProvider: KeyProvider): PayloadCipher = AesGcmPayloadCipher(keyProvider)

    @Provides
    @Singleton
    fun provideMockBackend(): MockBackend = MockBackend()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        mockBackend: MockBackend,
        cipher: PayloadCipher,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(MockBackendInterceptor(mockBackend, cipher))
            .build()

    @Provides
    @Singleton
    fun providePaymentGateway(
        client: OkHttpClient,
        cipher: PayloadCipher,
    ): PaymentGateway = HttpPaymentGateway(client, BuildConfig.API_BASE_URL.toHttpUrl(), cipher)
}
