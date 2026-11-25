package com.example.unifit.model

// Um exercício dentro de uma ficha (fica na lista "exercicios" do documento da ficha).
// Os valores padrão são obrigatórios para o Firestore conseguir criar o objeto (toObject).
data class ItemExercicio(
    val exercicioId: String = "",
    val nome: String = "",
    val series: Int = 0,
    val repeticoes: Int = 0,
    val cargaKg: Double = 0.0, // 0 = peso corporal
    val ordem: Int = 0
)
