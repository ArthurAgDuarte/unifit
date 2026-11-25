package com.example.unifit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.unifit.ui.base.BaseActivity

// Base das telas antigas (fora do escopo da integração Firebase).
// A fonte maior e a recriação ao mudar a opção vêm da BaseActivity.
// O alto contraste aqui continua sendo o ajuste de cores abaixo, porque estas telas
// usam cores fixas nos XMLs e ficariam ilegíveis com o tema escuro.
open class TelaBase : BaseActivity() {

    override val usaTemaDeAltoContraste: Boolean
        get() = false

    override val configuraBarrasDoSistema: Boolean
        get() = false

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (altoContrasteAplicado) {
            val raiz = findViewById<View>(android.R.id.content)
            aplicarAltoContraste(raiz)
            // Itens criados depois (listas preenchidas no código) também recebem as cores
            raiz.viewTreeObserver.addOnGlobalLayoutListener { aplicarAltoContraste(raiz) }
        }
    }

    // Escurece os textos cinza: deixa tudo com mais contraste para leitura
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
