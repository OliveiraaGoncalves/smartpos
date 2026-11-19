package com.raimundo.smartpos.di

import com.raimundo.smartpos.BuildConfig
import com.raimundo.smartpos.core.domain.gateway.PaymentGateway
import com.raimundo.smartpos.core.network.HttpPaymentGateway
import com.raimundo.smartpos.core.security.AesGcmPayloadCipher
import com.raimundo.smartpos.core.security.AndroidKeystoreKeyProvider
import com.raimundo.smartpos.core.security.KeyProvider
import com.raimundo.smartpos.core.security.PayloadCipher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Flavor `prod`: backend real via HTTPS; a chave fica só no Android Keystore (nunca em
 * texto claro). Certificate pinning é o próximo passo documentado no README.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProdNetworkModule {
    @Provides
    @Singleton
    fun provideKeyProvider(): KeyProvider = AndroidKeystoreKeyProvider()

    @Provides
    @Singleton
    fun providePayloadCipher(keyProvider: KeyProvider): PayloadCipher = AesGcmPayloadCipher(keyProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun providePaymentGateway(
        client: OkHttpClient,
        cipher: PayloadCipher,
    ): PaymentGateway = HttpPaymentGateway(client, BuildConfig.API_BASE_URL.toHttpUrl(), cipher)
}
