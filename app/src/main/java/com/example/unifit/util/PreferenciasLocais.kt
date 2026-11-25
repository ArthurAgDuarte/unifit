package com.example.unifit.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.unifit.model.Preferencias

// Cópia local (SharedPreferences) do que precisa ser lido rápido ao abrir o app:
// a flag do onboarding e as opções de acessibilidade.
// Usa o mesmo arquivo e as mesmas chaves da TelaBase, para as telas antigas enxergarem os valores.
object PreferenciasLocais {

    private const val ARQUIVO = "unifit"
    private const val CHAVE_ONBOARDING_VISTO = "onboarding_visto"
    private const val CHAVE_VLIBRAS = "vlibras"
    private const val CHAVE_FONTE_MAIOR = "fonte_maior"
    private const val CHAVE_ALTO_CONTRASTE = "alto_contraste"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun onboardingVisto(context: Context): Boolean =
        prefs(context).getBoolean(CHAVE_ONBOARDING_VISTO, false)

    fun marcarOnboardingVisto(context: Context) {
        prefs(context).edit { putBoolean(CHAVE_ONBOARDING_VISTO, true) }
    }

    fun lerAcessibilidade(context: Context): Preferencias {
        val p = prefs(context)
        return Preferencias(
            fonteMaior = p.getBoolean(CHAVE_FONTE_MAIOR, false),
            altoContraste = p.getBoolean(CHAVE_ALTO_CONTRASTE, false),
            vlibras = p.getBoolean(CHAVE_VLIBRAS, false)
        )
    }

    fun salvarAcessibilidade(context: Context, preferencias: Preferencias) {
        prefs(context).edit {
            putBoolean(CHAVE_FONTE_MAIOR, preferencias.fonteMaior)
            putBoolean(CHAVE_ALTO_CONTRASTE, preferencias.altoContraste)
            putBoolean(CHAVE_VLIBRAS, preferencias.vlibras)
        }
    }
}
