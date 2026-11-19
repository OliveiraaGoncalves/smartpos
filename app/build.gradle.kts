plugins {
    // AGP 9+ compila Kotlin nativamente; não aplicamos org.jetbrains.kotlin.android.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.smartpos.quality)
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
        testInstrumentationRunner = "com.raimundo.smartpos.HiltTestRunner"

        buildConfigField("String", "MERCHANT_ID", "\"123456789012345\"")
        buildConfigField("String", "TERMINAL_ID", "\"12345678\"")
        buildConfigField("String", "PAYLOAD_KEY_ID", "\"dev-key-01\"")
    }

    flavorDimensions += "backend"
    productFlavors {
        // Backend simulado em processo (MockBackendInterceptor do core:network-mock). Sem servidor.
        create("mock") {
            dimension = "backend"
            buildConfigField("String", "API_BASE_URL", "\"https://mock.smartpos.local/\"")
            // Chave AES-256 APENAS de desenvolvimento, compartilhada com o mock (ver README > Segurança).
            buildConfigField(
                "String",
                "DEV_PAYLOAD_KEY_HEX",
                "\"5405c7d451bf3529d8d4581174490f75498db8c98f71a8efc09605a49651178c\"",
            )
        }
        // Backend real: HttpPaymentGateway fala HTTPS de verdade; chave vem do Android Keystore.
        create("prod") {
            dimension = "backend"
            buildConfigField("String", "API_BASE_URL", "\"https://api.smartpos.example.com/\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:security"))
    implementation(project(":core:network"))
    implementation(project(":core:local-storage"))
    implementation(project(":core:data"))
    implementation(project(":core:printer"))
    implementation(project(":core:ui"))

    implementation(project(":feature:home"))
    implementation(project(":feature:sale"))
    implementation(project(":feature:transaction-detail"))
    implementation(project(":feature:receipt"))
    implementation(project(":feature:transactions"))
    implementation(project(":feature:pendings"))
    implementation(project(":feature:more"))
    implementation(project(":feature:blocking"))

    "mockImplementation"(project(":core:network-mock"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:testing"))

    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
