package com.example.unifit.data

import android.util.Log
import com.example.unifit.util.Constantes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

// Acesso central ao Firebase: instâncias, login anônimo e o documento do usuário de demonstração
object FirebaseHelper {

    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // users/ana: base das subcoleções progresso, notificacoes e preferencias
    val usuarioDemo: DocumentReference
        get() = db.collection(Constantes.COLECAO_USERS).document(Constantes.USUARIO_DEMO_ID)

    // Login anônimo: só serve para as regras de segurança aceitarem as leituras e escritas.
    // Se já existe uma sessão salva no aparelho, reaproveita sem ir à rede.
    fun entrarAnonimo(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (auth.currentUser != null) {
            onSuccess()
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener {
                Log.d(Constantes.TAG_FIREBASE, "Login anônimo feito: uid=${it.user?.uid}")
                onSuccess()
            }
            .addOnFailureListener { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Falha no login anônimo", erro)
                onError(erro)
            }
    }
}
