package com.example.unifit.data

import com.example.unifit.model.Ficha
import com.example.unifit.util.Constantes

// Leitura das fichas de treino (coleção "fichas")
class FichaRepository {

    private val fichas = FirebaseHelper.db.collection(Constantes.COLECAO_FICHAS)

    // Fichas do aluno de demonstração que caem num dia da semana (1 = segunda ... 7 = domingo)
    fun buscarFichasDoDia(
        diaSemana: Int,
        onSuccess: (List<Ficha>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        fichas
            .whereEqualTo(Constantes.CAMPO_ALUNO_ID, Constantes.USUARIO_DEMO_ID)
            .whereArrayContains(Constantes.CAMPO_DIAS_SEMANA, diaSemana)
            .get()
            .addOnSuccessListener { resultado ->
                // Ordena no app para não precisar de índice composto no Firestore
                val lista = resultado.toObjects(Ficha::class.java).sortedBy { it.nome }
                onSuccess(lista)
            }
            .addOnFailureListener { erro -> onError(erro) }
    }

    // Uma ficha pelo id, com os exercícios já ordenados por "ordem". Devolve null se não existir.
    fun buscarFicha(
        fichaId: String,
        onSuccess: (Ficha?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        fichas.document(fichaId)
            .get()
            .addOnSuccessListener { documento ->
                val ficha = documento.toObject(Ficha::class.java)
                onSuccess(ficha?.copy(exercicios = ficha.exercicios.sortedBy { it.ordem }))
            }
            .addOnFailureListener { erro -> onError(erro) }
    }
}
