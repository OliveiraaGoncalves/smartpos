plugins {
    // AGP 9+ tem suporte nativo a Kotlin: o plugin org.jetbrains.kotlin.android não é mais aplicado.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.kover)
}

/**
 * Relatório agregado de cobertura: um use case em `core:domain` muitas vezes só é
 * exercitado pelos testes do ViewModel que o consome (`feature:*`), então olhar o Kover
 * de `core:domain` isoladamente subestima a cobertura real. `core:data` fica de fora do
 * agregado: seus testes são instrumentados (Room/emulador) e o Kover, por padrão, só
 * instrumenta testes de JVM (ver README > Qualidade).
 */
dependencies {
    kover(project(":core:model"))
    kover(project(":core:common"))
    kover(project(":core:domain"))
    kover(project(":core:security"))
    kover(project(":core:network"))
    kover(project(":core:network-mock"))
    kover(project(":core:printer"))
    kover(project(":feature:home"))
    kover(project(":feature:sale"))
    kover(project(":feature:transaction-detail"))
    kover(project(":feature:transactions"))
    kover(project(":feature:pendings"))
    kover(project(":feature:receipt"))
    kover(project(":app"))
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                packages("com.raimundo.smartpos.di", "hilt_aggregated_deps", "dagger.hilt.internal.aggregatedroot.codegen")
                classes(
                    "*.*Screen",
                    "*.*ScreenKt",
                    "*.*Navigation*",
                    "*.*Route",
                    "*.*Route\$Companion",
                    "*MainActivity",
                    "*SmartPosApplication*",
                    "*SmartPosNavHost*",
                    "*ComposableSingletons*",
                    "*_Factory*",
                    "*_HiltModules*",
                    "*_MembersInjector",
                    "*_LazyMapKey",
                    "*\$InstanceHolder",
                    // Dependem de hardware/sistema real (Keystore, ConnectivityManager): cobertos
                    // manualmente em dispositivo real, não em JVM (ver README > Qualidade).
                    "*AndroidKeystoreKeyProvider",
                    "*ConnectivityManagerNetworkMonitor*",
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
