package com.example.unifit.util

import android.view.View
import androidx.annotation.StringRes
import com.example.unifit.R
import com.google.android.material.snackbar.Snackbar

// Snackbars padronizados do app
object Mensagens {

    // ancora: view acima da qual o Snackbar aparece (ex.: o menu inferior)
    fun mostrar(raiz: View, @StringRes mensagem: Int, ancora: View? = null) {
        Snackbar.make(raiz, mensagem, Snackbar.LENGTH_SHORT)
            .setAnchorView(ancora)
            .show()
    }

    // Para botões que levam a telas fora do escopo atual
    fun emBreve(raiz: View, ancora: View? = null) {
        mostrar(raiz, R.string.em_breve, ancora)
    }
}
