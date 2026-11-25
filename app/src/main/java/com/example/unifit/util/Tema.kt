package com.example.unifit.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.color.MaterialColors

// Lê uma cor do tema atual (ex.: R.attr.corDestaque), para o código seguir o alto contraste
object Tema {

    @ColorInt
    fun cor(context: Context, @AttrRes atributo: Int): Int =
        MaterialColors.getColor(context, atributo, Color.MAGENTA) // magenta = atributo faltando no tema
}
