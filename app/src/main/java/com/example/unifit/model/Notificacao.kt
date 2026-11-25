package com.example.unifit.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Documento users/ana/notificacoes/{id}
data class Notificacao(
    @DocumentId val id: String = "",
    val categoria: String = "", // ver Constantes.CATEGORIA_*
    val titulo: String = "",
    val descricao: String = "",
    val lida: Boolean = false,
    val criadoEm: Timestamp? = null
)
