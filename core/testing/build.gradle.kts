plugins {
    alias(libs.plugins.smartpos.jvm.library)
}

// Kotlin puro de propósito: core:domain (sem Android) também precisa destas fakes nos
// seus testes unitários, e um módulo jvm não pode depender de um módulo Android.
// Infra de teste específica de Android (HiltTestRunner, androidx-test) fica em :app.
dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:domain"))

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.truth)
    api(libs.turbine)
}
