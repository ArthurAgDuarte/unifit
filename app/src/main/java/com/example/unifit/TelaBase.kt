package com.example.unifit

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Chaves das opções de acessibilidade (ficam salvas mesmo depois de fechar o app)
const val VLIBRAS = "vlibras"
const val FONTE_MAIOR = "fonte_maior"
const val ALTO_CONTRASTE = "alto_contraste"

fun lerPreferencia(context: Context, chave: String): Boolean {
    return context.getSharedPreferences("unifit", Context.MODE_PRIVATE).getBoolean(chave, false)
}

fun salvarPreferencia(context: Context, chave: String, valor: Boolean) {
    context.getSharedPreferences("unifit", Context.MODE_PRIVATE).edit().putBoolean(chave, valor).apply()
}

// Todas as telas herdam desta classe para aplicar a "Fonte maior" e o "Alto contraste"
open class TelaBase : AppCompatActivity() {
    var fonteMaiorAplicada = false
    var altoContrasteAplicado = false

    override fun attachBaseContext(novoContexto: Context) {
        // Fonte maior: aumenta em 20% o tamanho de todos os textos (sp) da tela
        fonteMaiorAplicada = lerPreferencia(novoContexto, FONTE_MAIOR)
        if (fonteMaiorAplicada) {
            val configuracao = Configuration(novoContexto.resources.configuration)
            configuracao.fontScale *= 1.2f
            super.attachBaseContext(novoContexto.createConfigurationContext(configuracao))
        } else {
            super.attachBaseContext(novoContexto)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        altoContrasteAplicado = lerPreferencia(this, ALTO_CONTRASTE)
        if (altoContrasteAplicado) {
            val raiz = findViewById<View>(android.R.id.content)
            aplicarAltoContraste(raiz)
            // Itens criados depois (listas preenchidas no código) também recebem as cores
            raiz.viewTreeObserver.addOnGlobalLayoutListener { aplicarAltoContraste(raiz) }
        }
    }

    override fun onResume() {
        super.onResume()
        // Se a opção mudou na tela de Acessibilidade, recarrega esta tela para aplicar
        if (lerPreferencia(this, FONTE_MAIOR) != fonteMaiorAplicada ||
            lerPreferencia(this, ALTO_CONTRASTE) != altoContrasteAplicado
        ) {
            recreate()
        }
    }

    // Escurece os textos cinza e clareia nada: deixa tudo com mais contraste para leitura
    fun aplicarAltoContraste(view: View) {
        // Textos com cor por estado (ex.: menu inferior) ficam como estão
        if (view is TextView && !view.textColors.isStateful) {
            val novaCor = when (view.currentTextColor) {
                getColor(R.color.texto_secundario) -> getColor(R.color.contraste_texto_secundario)
                getColor(R.color.texto_principal) -> getColor(R.color.black)
                getColor(R.color.azul_texto_dica) -> getColor(R.color.azul_unifit)
                else -> null
            }
            if (novaCor != null) view.setTextColor(novaCor)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                aplicarAltoContraste(view.getChildAt(i))
            }
        }
    }
}
