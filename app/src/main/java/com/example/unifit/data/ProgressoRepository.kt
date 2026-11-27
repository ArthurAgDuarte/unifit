package com.example.unifit.data

import com.example.unifit.model.Progresso
import com.example.unifit.util.Constantes
import com.example.unifit.util.Datas
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

// Progresso do treino do dia (users/ana/progresso/{yyyy-MM-dd})
class ProgressoRepository {

    private fun documentoDeHoje(): DocumentReference =
        FirebaseHelper.usuarioDemo
            .collection(Constantes.COLECAO_PROGRESSO)
            .document(Datas.idDeHoje())

    // Devolve null se ainda não houve treino hoje
    fun buscarProgressoDeHoje(
        onSuccess: (Progresso?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        documentoDeHoje()
            .get()
            .addOnSuccessListener { documento -> onSuccess(documento.toObject(Progresso::class.java)) }
            .addOnFailureListener { erro -> onError(erro) }
    }

    // Cria ou atualiza o progresso de hoje com a ficha e o horário de início.
    // O merge mantém a lista exerciciosConcluidos que já existir.
    fun iniciarTreino(
        fichaId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val dados = mapOf(
            Constantes.CAMPO_FICHA_ID to fichaId,
            Constantes.CAMPO_INICIADO_EM to FieldValue.serverTimestamp()
        )
        documentoDeHoje()
            .set(dados, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { erro -> onError(erro) }
    }

    // Adiciona o id do exercício concluído à lista exerciciosConcluidos do progresso de hoje
    fun concluirExercicio(
        exercicioId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        documentoDeHoje()
            .update("exerciciosConcluidos", FieldValue.arrayUnion(exercicioId))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                val dados = mapOf("exerciciosConcluidos" to listOf(exercicioId))
                documentoDeHoje()
                    .set(dados, SetOptions.merge())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { erro -> onError(erro) }
            }
    }
}
