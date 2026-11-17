package com.example.unifit

// Dados das telas do administrador (ficam só na memória, como as outras listas do app)

// Um usuário do sistema (RF22)
data class Usuario(
    val nome: String,
    val detalhe: String, // "Mat. 2023104587 • Aluna" ou "Professor • Spinning"
    val professor: Boolean,
    var ativo: Boolean
)

val listaUsuarios = mutableListOf(
    Usuario("Ana Beatriz Souza", "Mat. 2023104587 • Aluna", false, true),
    Usuario("Carlos Mendes", "Professor • Spinning", true, true),
    Usuario("Júlia Ramos", "Mat. 2022208811 • Aluna", false, false),
    Usuario("Pedro Henrique Lima", "Mat. 2024100342 • Aluno", false, true),
    Usuario("Marina Costa", "Professora • Yoga", true, true),
    Usuario("Rafael Tavares", "Mat. 2021307765 • Aluno", false, false),
    Usuario("Rafael Oliveira", "Professor • Funcional", true, true),
    Usuario("Júlia Andrade", "Professora • Pilates", true, true),
    Usuario("Lia Ferreira", "Professora • Zumba", true, true)
)

// Um exercício do catálogo da academia (RF24)
data class ExercicioCatalogo(
    val nome: String,
    val grupo: String,
    val equipamento: String
)

val gruposMusculares = arrayOf("Peitoral", "Costas", "Pernas", "Ombros", "Bíceps", "Tríceps", "Abdômen")

val listaExerciciosCatalogo = mutableListOf(
    ExercicioCatalogo("Supino reto com barra", "Peitoral", "Barra livre"),
    ExercicioCatalogo("Agachamento livre", "Pernas", "Barra livre"),
    ExercicioCatalogo("Puxada frontal", "Costas", "Polia alta"),
    ExercicioCatalogo("Rosca direta", "Bíceps", "Halteres"),
    ExercicioCatalogo("Leg press 45°", "Pernas", "Máquina"),
    ExercicioCatalogo("Tríceps na polia", "Tríceps", "Polia"),
    ExercicioCatalogo("Desenvolvimento c/ halteres", "Ombros", "Halteres"),
    ExercicioCatalogo("Abdominal na prancha", "Abdômen", "Peso corporal")
)

// Um aparelho da academia (RF24, RF25)
data class Aparelho(
    val nome: String,
    val area: String,
    var disponivel: Boolean
)

val listaAparelhos = mutableListOf(
    Aparelho("Leg Press 45°", "Área de Musculação", true),
    Aparelho("Esteira #3", "Cardio", false),
    Aparelho("Supino reto", "Área de Musculação", true),
    Aparelho("Crossover", "Área de Musculação", false),
    Aparelho("Bike de spinning", "Sala de Ciclismo", true),
    Aparelho("Cadeira extensora", "Área de Musculação", true)
)
