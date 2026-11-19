plugins {
    // AGP 9+ compila Kotlin nativamente; não aplicamos org.jetbrains.kotlin.android.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.raimundo.smartpos"
    // compileSdk 37 é exigido pelas libs AndroidX atuais; targetSdk 36 é o mínimo hoje na Play Store.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.raimundo.smartpos"
        // SmartPOS de mercado ainda rodam Android 7.x – 10; minSdk 24 cobre o parque.
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // HTTPS obrigatório: o HttpPaymentGateway rejeita URLs que não sejam https.
        buildConfigField("String", "API_BASE_URL", "\"https://mock.smartpos.local/\"")
        // true = backend simulado em processo (interceptor OkHttp). Em produção: false + URL real.
        buildConfigField("boolean", "USE_MOCK_BACKEND", "true")
        buildConfigField("String", "MERCHANT_ID", "\"123456789012345\"")
        buildConfigField("String", "TERMINAL_ID", "\"12345678\"")
        buildConfigField("String", "PAYLOAD_KEY_ID", "\"dev-key-01\"")
        // Chave AES-256 APENAS de desenvolvimento (compartilhada com o mock).
        // Em produção a chave é injetada por terminal (ver README > Segurança).
        buildConfigField(
            "String",
            "DEV_PAYLOAD_KEY_HEX",
            "\"5405c7d451bf3529d8d4581174490f75498db8c98f71a8efc09605a49651178c\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
