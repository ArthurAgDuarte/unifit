package com.example.unifit.model

import com.google.firebase.firestore.DocumentId

// Documento fichas/{fichaId}
data class Ficha(
    // Preenchido com o id do documento na leitura; não é gravado como campo
    @DocumentId val id: String = "",
    val alunoId: String = "",
    val nome: String = "",
    val diasSemana: List<Int> = emptyList(), // 1 = segunda ... 7 = domingo
    val duracaoMin: Int = 0,
    val dificuldade: String = "",
    val exercicios: List<ItemExercicio> = emptyList()
)
