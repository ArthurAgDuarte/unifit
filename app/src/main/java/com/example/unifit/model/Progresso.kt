package com.example.unifit.model

import com.google.firebase.Timestamp

// Documento users/ana/progresso/{yyyy-MM-dd}: o treino do dia
data class Progresso(
    val fichaId: String = "",
    val iniciadoEm: Timestamp? = null,
    val exerciciosConcluidos: List<String> = emptyList() // ids dos exercícios (exercicioId)
)
