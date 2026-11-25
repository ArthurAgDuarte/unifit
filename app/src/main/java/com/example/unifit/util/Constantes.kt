package com.example.unifit.util

// Valores fixos usados no app inteiro
object Constantes {

    // Como o login ainda não existe, todos os dados ficam neste usuário de demonstração
    const val USUARIO_DEMO_ID = "ana"

    // Coleções e documentos do Firestore
    const val COLECAO_USERS = "users"
    const val COLECAO_FICHAS = "fichas"
    const val COLECAO_PROGRESSO = "progresso"
    const val COLECAO_NOTIFICACOES = "notificacoes"
    const val COLECAO_PREFERENCIAS = "preferencias"

    // O Firestore exige um id de documento: as preferências ficam em users/ana/preferencias/acessibilidade
    const val DOC_PREFERENCIAS = "acessibilidade"

    // Campos usados em consultas e atualizações
    const val CAMPO_ALUNO_ID = "alunoId"
    const val CAMPO_DIAS_SEMANA = "diasSemana"
    const val CAMPO_FICHA_ID = "fichaId"
    const val CAMPO_INICIADO_EM = "iniciadoEm"
    const val CAMPO_LIDA = "lida"
    const val CAMPO_CRIADO_EM = "criadoEm"

    // Categorias de notificação
    const val CATEGORIA_AULA_CONFIRMADA = "aula_confirmada"
    const val CATEGORIA_AULA_CANCELADA = "aula_cancelada"
    const val CATEGORIA_HIDRATACAO = "hidratacao"
    const val CATEGORIA_FICHA_ATUALIZADA = "ficha_atualizada"
    const val CATEGORIA_CONQUISTA = "conquista"

    // Dificuldades possíveis de uma ficha
    const val DIFICULDADE_INICIANTE = "Iniciante"
    const val DIFICULDADE_INTERMEDIARIO = "Intermediário"
    const val DIFICULDADE_AVANCADO = "Avançado"

    // Extras de Intent
    const val EXTRA_FICHA_ID = "fichaId"

    // Tag dos logs do Firebase (Logcat)
    const val TAG_FIREBASE = "UniFitFirebase"
}
