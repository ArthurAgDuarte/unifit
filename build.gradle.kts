// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Plugin do Firebase: lê o app/google-services.json
    alias(libs.plugins.google.services) apply false
    // Não é aplicado em nenhum módulo (o AGP 9 já tem Kotlin embutido):
    // só obriga o build a usar esta versão do compilador Kotlin
    alias(libs.plugins.kotlin.android) apply false
}
