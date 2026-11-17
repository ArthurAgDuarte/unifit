package com.example.unifit

// Uma aula coletiva da academia da universidade (RF12, RF13)
data class Aula(
    val nome: String,
    val categoria: String,
    val professor: String,
    val diaOffset: Int, // 0 = hoje, 1 = amanhã, 2 = depois de amanhã...
    val horario: String,
    val local: String,
    val vagas: Int,
    var inscritos: Int,
    var reservada: Boolean = false,
    val icone: Int = R.drawable.ic_atividade
) {
    fun vagasRestantes() = vagas - inscritos
    fun lotada() = vagasRestantes() <= 0
}

// Uma notificação da tela Notificações
data class Notificacao(
    val titulo: String,
    val mensagem: String,
    val quando: String,
    val dia: String, // "HOJE" ou "ONTEM"
    val icone: Int,
    var lida: Boolean = false
)

val categoriasAulas = listOf("Todas", "Spinning", "Yoga", "Funcional", "Pilates", "Zumba")

// Aulas como no protótipo (pág. 31). A de Spinning já está reservada (aparece no Início e em Minhas Reservas)
val listaAulas = mutableListOf(
    Aula("Spinning", "Spinning", "Prof. Carlos", 0, "18:30", "Sala de Ciclismo", 20, 16, true, R.drawable.ic_atividade),
    Aula("Yoga", "Yoga", "Prof.ª Marina", 0, "19:00", "Sala 1", 20, 8, false, R.drawable.ic_brilho),
    Aula("Funcional", "Funcional", "Prof. Rafael", 0, "20:00", "Área Funcional", 20, 20, false, R.drawable.ic_haltere),
    Aula("Pilates", "Pilates", "Prof.ª Júlia", 0, "20:30", "Sala 2", 12, 4, false, R.drawable.ic_pessoa_em_pe),
    Aula("Zumba", "Zumba", "Prof.ª Lia", 1, "07:30", "Sala 1", 25, 10, false, R.drawable.ic_atividade),
    Aula("Spinning", "Spinning", "Prof. Carlos", 1, "18:30", "Sala de Ciclismo", 20, 9, false, R.drawable.ic_atividade),
    Aula("Yoga", "Yoga", "Prof.ª Marina", 2, "19:00", "Sala 1", 20, 12, true, R.drawable.ic_brilho),
    Aula("Funcional", "Funcional", "Prof. Rafael", 3, "07:00", "Área Funcional", 20, 11, false, R.drawable.ic_haltere),
    Aula("Pilates", "Pilates", "Prof.ª Júlia", 4, "20:30", "Sala 2", 12, 6, false, R.drawable.ic_pessoa_em_pe)
)

// Ícone de cada categoria (usado também nas aulas criadas pelo admin)
fun iconeDaCategoria(categoria: String): Int {
    return when (categoria) {
        "Yoga" -> R.drawable.ic_brilho
        "Funcional" -> R.drawable.ic_haltere
        "Pilates" -> R.drawable.ic_pessoa_em_pe
        "Spinning", "Zumba" -> R.drawable.ic_atividade
        else -> R.drawable.ic_calendario_contorno
    }
}

// Texto curto sobre cada modalidade (Detalhe da Aula)
fun descricaoDaCategoria(categoria: String): String {
    return when (categoria) {
        "Spinning" -> "Aula de ciclismo indoor com ritmo e música. Trabalha resistência e queima calórica. Leve toalha e garrafa de água."
        "Yoga" -> "Posturas, respiração e alongamento para ganhar flexibilidade e reduzir o estresse. Use roupas confortáveis."
        "Funcional" -> "Circuito com movimentos do dia a dia, cordas e pesos livres. Melhora força, equilíbrio e condicionamento."
        "Pilates" -> "Exercícios no solo com foco em core, postura e controle do movimento."
        "Zumba" -> "Aula de dança com ritmos latinos para movimentar o corpo e se divertir."
        else -> "Aula coletiva da academia da Unifor."
    }
}

val listaNotificacoes = listOf(
    Notificacao("Aula confirmada", "Spinning hoje às 18:30", "há 10 min", "HOJE", R.drawable.ic_calendario_contorno),
    Notificacao("Hora de se hidratar", "1,6 L de 2,5 L hoje", "há 1 h", "HOJE", R.drawable.ic_gota),
    Notificacao("Nova ficha disponível", "Prof. Rafael atualizou seu treino", "há 3 h", "HOJE", R.drawable.ic_haltere),
    Notificacao("Conquista desbloqueada", "5 treinos na semana", "15 set", "ONTEM", R.drawable.ic_trofeu, true),
    Notificacao("Aula cancelada", "Pilates das 20:30 foi cancelada", "15 set", "ONTEM", R.drawable.ic_fechar, true)
)

// Dados do perfil (editáveis na tela Perfil)
var nomeUsuario = "Aluno UniFit"
var cursoUsuario = "Educação Física"
var emailUsuario = "aluno@unifor.br"
var telefoneUsuario = "(85) 98765-4321"
val matriculaUsuario = "2023104587" // não editável (RF19.1.3)
var lembretesAtivos = true

// Iniciais do nome para o avatar: primeira letra do primeiro e do último nome
fun iniciaisDe(nome: String): String {
    val partes = nome.trim().split(" ").filter { it.isNotEmpty() }
    if (partes.isEmpty()) return "?"
    val iniciais = if (partes.size > 1) "${partes.first()[0]}${partes.last()[0]}" else partes.first().take(2)
    return iniciais.uppercase()
}
