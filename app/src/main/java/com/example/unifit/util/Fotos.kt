package com.example.unifit.util

import androidx.annotation.DrawableRes
import com.example.unifit.R

// As fotos ficam no app (o Firestore não guarda imagem): a ficha usa a foto pela posição na lista
object Fotos {

    private val fotosDeFicha = listOf(
        R.drawable.foto_treino_a,
        R.drawable.foto_treino_b,
        R.drawable.foto_treino_c
    )

    // 1ª ficha -> foto A, 2ª -> B, 3ª -> C, 4ª volta para A...
    @DrawableRes
    fun daFicha(posicao: Int): Int = fotosDeFicha[posicao % fotosDeFicha.size]

    // Todos os exercícios usam a mesma foto
    @DrawableRes
    fun doExercicio(): Int = R.drawable.foto_exercicio
}
