package com.example.unifit.util

import android.content.Context
import com.example.unifit.R
import java.text.NumberFormat
import java.util.Locale

// Formatação de números para exibição (padrão brasileiro)
object Formatos {

    private val localeBrasil: Locale = Locale.forLanguageTag("pt-BR")

    // 60.0 -> "60 kg" | 22.5 -> "22,5 kg" | 0.0 -> "Peso corporal"
    fun carga(context: Context, cargaKg: Double): String {
        if (cargaKg <= 0.0) return context.getString(R.string.carga_peso_corporal)
        val numero = NumberFormat.getInstance(localeBrasil).apply { maximumFractionDigits = 1 }
        return context.getString(R.string.carga_kg, numero.format(cargaKg))
    }
}
