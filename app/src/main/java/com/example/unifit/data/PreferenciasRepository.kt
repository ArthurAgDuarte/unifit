package com.example.unifit.data

import com.example.unifit.model.Preferencias
import com.example.unifit.util.Constantes
import com.google.firebase.firestore.DocumentReference

// Preferências de acessibilidade no Firestore (users/ana/preferencias/acessibilidade).
// A cópia local em SharedPreferences fica no pacote util (Etapa 2).
class PreferenciasRepository {

    private val documento: DocumentReference =
        FirebaseHelper.usuarioDemo
            .collection(Constantes.COLECAO_PREFERENCIAS)
            .document(Constantes.DOC_PREFERENCIAS)

    // Se o documento ainda não existe, devolve tudo desligado
    fun carregar(
        onSuccess: (Preferencias) -> Unit,
        onError: (Exception) -> Unit
    ) {
        documento
            .get()
            .addOnSuccessListener { doc -> onSuccess(doc.toObject(Preferencias::class.java) ?: Preferencias()) }
            .addOnFailureListener { erro -> onError(erro) }
    }

    fun salvar(
        preferencias: Preferencias,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        documento
            .set(preferencias)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { erro -> onError(erro) }
    }
}
