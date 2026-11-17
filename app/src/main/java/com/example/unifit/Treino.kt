package com.example.unifit

import java.time.LocalDate

// Um exercício dentro de uma ficha
data class Exercicio(
    val nome: String,
    val series: Int,
    val repeticoes: Int,
    val carga: String
)

// Uma ficha de treino (Treino A, B, C...)
data class Ficha(
    var nome: String,
    var duracaoMin: Int,
    var nivel: String,
    val imagem: Int,
    val exercicios: MutableList<Exercicio>,
    val concluidos: MutableSet<Int> = mutableSetOf() // posições dos exercícios já concluídos (RF10.3)
)

// Um treino que o usuário terminou (usado nos números do Início e do Perfil)
data class TreinoConcluido(
    val nomeFicha: String,
    val data: LocalDate,
    val minutos: Int
)

val niveis = arrayOf("Iniciante", "Intermediário", "Avançado")

// Fichas de exemplo (ficam só na memória: somem ao fechar o app)
val listaFichas = mutableListOf(
    Ficha(
        "Treino A — Peito e Tríceps", 45, "Intermediário", R.drawable.foto_treino_a,
        mutableListOf(
            Exercicio("Supino reto com barra", 4, 10, "60 kg"),
            Exercicio("Supino inclinado c/ halteres", 3, 12, "22 kg"),
            Exercicio("Crucifixo na máquina", 3, 12, "35 kg"),
            Exercicio("Tríceps na polia", 4, 12, "25 kg"),
            Exercicio("Tríceps francês", 3, 10, "14 kg"),
            Exercicio("Crossover", 3, 15, "15 kg"),
            Exercicio("Paralelas", 3, 10, "Peso corporal"),
            Exercicio("Tríceps coice", 3, 12, "8 kg")
        ),
        mutableSetOf(0, 1, 2) // 3 de 8 concluídos, como no protótipo
    ),
    Ficha(
        "Treino B — Costas e Bíceps", 40, "Intermediário", R.drawable.foto_treino_b,
        mutableListOf(
            Exercicio("Puxada frontal", 4, 10, "50 kg"),
            Exercicio("Remada curvada", 4, 10, "40 kg"),
            Exercicio("Remada baixa", 3, 12, "45 kg"),
            Exercicio("Pulldown", 3, 12, "30 kg"),
            Exercicio("Rosca direta", 4, 10, "20 kg"),
            Exercicio("Rosca martelo", 3, 12, "12 kg"),
            Exercicio("Rosca concentrada", 3, 12, "10 kg")
        )
    ),
    Ficha(
        "Treino C — Pernas e Ombros", 55, "Avançado", R.drawable.foto_treino_c,
        mutableListOf(
            Exercicio("Agachamento livre", 4, 10, "70 kg"),
            Exercicio("Leg press", 4, 12, "150 kg"),
            Exercicio("Cadeira extensora", 3, 12, "40 kg"),
            Exercicio("Mesa flexora", 3, 12, "35 kg"),
            Exercicio("Panturrilha em pé", 4, 15, "60 kg"),
            Exercicio("Desenvolvimento c/ halteres", 4, 10, "16 kg"),
            Exercicio("Elevação lateral", 3, 12, "8 kg"),
            Exercicio("Elevação frontal", 3, 12, "8 kg"),
            Exercicio("Encolhimento", 3, 15, "24 kg")
        )
    )
)

val historicoTreinos = mutableListOf<TreinoConcluido>()

// ---------- Detalhe do Exercício (RF10) ----------

// Informações extras de cada exercício: músculos trabalhados e passo a passo
data class InfoExercicio(
    val musculos: String,
    val passos: List<String>,
    val duracaoVideo: String = "0:45"
)

val infoExercicios = mapOf(
    "Supino reto com barra" to InfoExercicio(
        "Peitoral maior • Tríceps • Deltoide anterior",
        listOf(
            "Deite no banco com os pés firmes no chão e escápulas encaixadas.",
            "Desça a barra controlando até a linha do peito.",
            "Empurre a barra para cima sem travar os cotovelos."
        )
    ),
    "Supino inclinado c/ halteres" to InfoExercicio(
        "Peitoral superior • Deltoide anterior • Tríceps",
        listOf(
            "Ajuste o banco entre 30° e 45° e apoie bem as costas.",
            "Desça os halteres abrindo os cotovelos até a altura do peito.",
            "Suba aproximando os halteres no alto, sem bater um no outro."
        )
    ),
    "Crucifixo na máquina" to InfoExercicio(
        "Peitoral maior • Deltoide anterior",
        listOf(
            "Ajuste o banco para as pegadas ficarem na linha do peito.",
            "Feche os braços em arco, contraindo o peitoral.",
            "Volte devagar até sentir o alongamento do peito."
        )
    ),
    "Tríceps na polia" to InfoExercicio(
        "Tríceps braquial",
        listOf(
            "Segure a barra com os cotovelos colados ao corpo.",
            "Estenda os braços até o fim, sem mexer os ombros.",
            "Volte controlando até os antebraços ficarem paralelos ao chão."
        )
    ),
    "Agachamento livre" to InfoExercicio(
        "Quadríceps • Glúteos • Posteriores de coxa",
        listOf(
            "Apoie a barra no trapézio com os pés na largura dos ombros.",
            "Desça levando o quadril para trás, mantendo a coluna neutra.",
            "Suba empurrando o chão com o pé inteiro."
        )
    ),
    "Puxada frontal" to InfoExercicio(
        "Dorsais • Bíceps • Romboides",
        listOf(
            "Segure a barra com pegada aberta e prenda as coxas no apoio.",
            "Puxe a barra até a parte de cima do peito, abrindo o peito.",
            "Volte devagar até esticar os braços."
        )
    ),
    "Rosca direta" to InfoExercicio(
        "Bíceps braquial • Braquial",
        listOf(
            "Fique em pé com os cotovelos junto ao corpo.",
            "Suba a carga dobrando apenas os cotovelos.",
            "Desça controlando, sem balançar o tronco."
        )
    ),
    "Leg press" to InfoExercicio(
        "Quadríceps • Glúteos",
        listOf(
            "Apoie bem as costas e os pés no centro da plataforma.",
            "Desça até os joelhos formarem cerca de 90°.",
            "Empurre a plataforma sem travar os joelhos no final."
        )
    )
)

// Passos usados quando o exercício não tem informação própria
fun infoDe(nomeExercicio: String): InfoExercicio {
    return infoExercicios[nomeExercicio] ?: InfoExercicio(
        "Grupo muscular do treino",
        listOf(
            "Ajuste o aparelho ou a carga antes de começar.",
            "Execute o movimento de forma controlada, sem pressa.",
            "Solte o ar na fase de esforço e puxe na volta."
        )
    )
}

// ---------- Histórico Fitness (RF16) ----------

// Uma atividade feita pelo aluno (treino ou aula)
data class AtividadeHistorico(
    val titulo: String,
    val diasAtras: Int, // 0 = hoje, 1 = ontem...
    val minutos: Int,
    val info: String,
    val icone: Int
)

val historicoAtividades = listOf(
    AtividadeHistorico("Treino A — Peito e Tríceps", 0, 45, "8 exercícios", R.drawable.ic_haltere),
    AtividadeHistorico("Spinning", 1, 45, "410 kcal", R.drawable.ic_atividade),
    AtividadeHistorico("Treino C — Pernas", 1, 52, "9 exercícios", R.drawable.ic_haltere),
    AtividadeHistorico("Yoga", 2, 60, "Flexibilidade", R.drawable.ic_brilho),
    AtividadeHistorico("Treino B — Costas e Bíceps", 5, 40, "7 exercícios", R.drawable.ic_haltere),
    AtividadeHistorico("Funcional", 12, 50, "520 kcal", R.drawable.ic_haltere),
    AtividadeHistorico("Treino A — Peito e Tríceps", 20, 47, "8 exercícios", R.drawable.ic_haltere),
    AtividadeHistorico("Pilates", 45, 55, "Mobilidade", R.drawable.ic_pessoa_em_pe),
    AtividadeHistorico("Treino C — Pernas", 70, 50, "9 exercícios", R.drawable.ic_haltere)
)
