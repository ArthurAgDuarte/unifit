package com.example.unifit.data

import com.example.unifit.model.Notificacao
import com.example.unifit.util.Constantes
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

// Notificações do usuário de demonstração (users/ana/notificacoes)
class NotificacaoRepository {

    private val notificacoes = FirebaseHelper.usuarioDemo.collection(Constantes.COLECAO_NOTIFICACOES)

    // Escuta a lista em tempo real, das mais novas para as mais antigas.
    // Quem chamar deve guardar o retorno e chamar remove() em onStop/onDestroyView.
    fun ouvirNotificacoes(
        onChange: (List<Notificacao>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return notificacoes
            .orderBy(Constantes.CAMPO_CRIADO_EM, Query.Direction.DESCENDING)
            .addSnapshotListener { resultado, erro ->
                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }
                onChange(resultado?.toObjects(Notificacao::class.java) ?: emptyList())
            }
    }

    // Escuta só o total de não lidas (badge do sino)
    fun ouvirTotalNaoLidas(
        onChange: (Int) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return notificacoes
            .whereEqualTo(Constantes.CAMPO_LIDA, false)
            .addSnapshotListener { resultado, erro ->
                if (erro != null) {
                    onError(erro)
                    return@addSnapshotListener
                }
                onChange(resultado?.size() ?: 0)
            }
    }

    fun marcarComoLida(
        notificacaoId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        notificacoes.document(notificacaoId)
            .update(Constantes.CAMPO_LIDA, true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { erro -> onError(erro) }
    }

    // Busca as não lidas e marca todas de uma vez com um WriteBatch
    fun marcarTodasComoLidas(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        notificacoes
            .whereEqualTo(Constantes.CAMPO_LIDA, false)
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }
                val lote = FirebaseHelper.db.batch()
                for (documento in resultado.documents) {
                    lote.update(documento.reference, Constantes.CAMPO_LIDA, true)
                }
                lote.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { erro -> onError(erro) }
            }
            .addOnFailureListener { erro -> onError(erro) }
    }
}
