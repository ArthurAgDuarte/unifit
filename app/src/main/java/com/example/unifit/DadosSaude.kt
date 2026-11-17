package com.example.unifit

// Um indicador de saúde da tela Indicadores de Saúde (RF17)
data class Indicador(
    val nome: String,
    val unidade: String,
    var valor: Double,
    val meta: Double,
    val inicial: Double, // só usado no peso: de onde o aluno começou
    val icone: Int,
    val cor: Int, // cor do ícone, da barra e do percentual
    val corFundo: Int, // fundo do círculo do ícone
    val somaAoAdicionar: Boolean, // calorias/água somam; peso substitui
    val dica: String // dica mostrada quando este é o indicador mais atrasado (RF17.3)
) {
    // Percentual da meta atingido (0 a 100)
    fun percentual(): Int {
        val fracao = if (nome == "Peso") (inicial - valor) / (inicial - meta) else valor / meta
        return (fracao.coerceIn(0.0, 1.0) * 100).toInt()
    }
}

// Valores como no protótipo (pág. 36)
val listaIndicadores = listOf(
    Indicador("Peso", "kg", 64.2, 62.0, 70.0, R.drawable.ic_peso, R.color.verde_hoje_texto, R.color.verde_hoje_fundo, false,
        "Mantenha a constância nos treinos para chegar na sua meta de peso."),
    Indicador("Calorias", "kcal", 1840.0, 2200.0, 0.0, R.drawable.ic_chama, R.color.amarelo, R.color.amarelo_claro, true,
        "Inclua um lanche com proteína para chegar na sua meta de calorias."),
    Indicador("Hidratação", "L", 1.6, 2.5, 0.0, R.drawable.ic_gota, R.color.azul_unifit, R.color.azul_claro, true,
        "Beba água a cada 2 horas para bater sua meta de hidratação.")
)
