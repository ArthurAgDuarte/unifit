package com.example.unifit.ui.base

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unifit.R
import com.example.unifit.util.PreferenciasLocais

// Todas as Activities herdam desta classe. Ela aplica as opções de acessibilidade salvas no aparelho:
// - Fonte maior: fontScale × 1,2 (em attachBaseContext, antes de a tela existir)
// - Alto contraste: Theme.UniFit.AltoContraste (setTheme antes do super.onCreate)
// Se a opção mudar enquanto a tela está aberta (em outra tela), ela se recria ao voltar (onResume).
abstract class BaseActivity : AppCompatActivity() {

    protected var fonteMaiorAplicada = false
        private set
    protected var altoContrasteAplicado = false
        private set

    // As telas antigas (TelaBase) usam cores fixas: o tema escuro deixaria textos ilegíveis nelas
    protected open val usaTemaDeAltoContraste: Boolean
        get() = true

    // As telas antigas configuram as barras do sistema por conta própria
    protected open val configuraBarrasDoSistema: Boolean
        get() = true

    // true = fundo escuro atrás da barra de status (ícones claros)
    protected open val fundoEscuro: Boolean
        get() = altoContrasteAplicado && usaTemaDeAltoContraste

    override fun attachBaseContext(novoContexto: Context) {
        fonteMaiorAplicada = PreferenciasLocais.lerAcessibilidade(novoContexto).fonteMaior
        if (fonteMaiorAplicada) {
            val configuracao = Configuration(novoContexto.resources.configuration)
            configuracao.fontScale *= ESCALA_FONTE_MAIOR
            super.attachBaseContext(novoContexto.createConfigurationContext(configuracao))
        } else {
            super.attachBaseContext(novoContexto)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        altoContrasteAplicado = PreferenciasLocais.lerAcessibilidade(this).altoContraste
        if (altoContrasteAplicado && usaTemaDeAltoContraste) {
            setTheme(R.style.Theme_UniFit_AltoContraste) // precisa vir antes do super.onCreate
        }
        super.onCreate(savedInstanceState)

        if (configuraBarrasDoSistema) {
            // Conteúdo atrás das barras do sistema, com ícones na cor certa para o fundo
            val estilo = if (fundoEscuro) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
            enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
        }
    }

    override fun onResume() {
        super.onResume()
        // A opção mudou na tela de Acessibilidade: recria esta tela para aplicar
        val atuais = PreferenciasLocais.lerAcessibilidade(this)
        if (atuais.fonteMaior != fonteMaiorAplicada || atuais.altoContraste != altoContrasteAplicado) {
            recreate()
        }
    }

    companion object {
        const val ESCALA_FONTE_MAIOR = 1.2f
    }
}
