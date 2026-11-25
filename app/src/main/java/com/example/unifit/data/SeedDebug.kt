package com.example.unifit.data

import android.util.Log
import com.example.unifit.BuildConfig
import com.example.unifit.model.Ficha
import com.example.unifit.model.ItemExercicio
import com.example.unifit.model.Notificacao
import com.example.unifit.model.Preferencias
import com.example.unifit.util.Constantes
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

// SÓ PARA DEBUG: popula o Firestore com os dados do protótipo.
// Usa ids fixos, então rodar de novo sobrescreve em vez de duplicar.
// A escrita em "fichas" precisa das regras abertas (ver firestore.rules, Etapa 7).
object SeedDebug {

    const val FICHA_A_ID = "treino_a"
    const val FICHA_B_ID = "treino_b"
    const val FICHA_C_ID = "treino_c"

    // Roda o seed só se a ficha A ainda não existir. onSuccess(true) = acabou de popular.
    fun popularSeVazio(onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        if (!BuildConfig.DEBUG) {
            onSuccess(false)
            return
        }
        FirebaseHelper.db.collection(Constantes.COLECAO_FICHAS).document(FICHA_A_ID)
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    onSuccess(false)
                } else {
                    popular(onSuccess = { onSuccess(true) }, onError = onError)
                }
            }
            .addOnFailureListener { erro -> onError(erro) }
    }

    // Grava tudo num único WriteBatch (ou grava tudo, ou nada)
    fun popular(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (!BuildConfig.DEBUG) return

        val db = FirebaseHelper.db
        val lote = db.batch()

        // users/ana
        lote.set(
            FirebaseHelper.usuarioDemo,
            mapOf(
                "nome" to "Ana Beatriz Souza",
                "matricula" to "2023104587",
                "curso" to "Engenharia"
            )
        )

        // fichas/{id} (o campo id não é gravado: vira o id do documento)
        for (ficha in fichas()) {
            lote.set(db.collection(Constantes.COLECAO_FICHAS).document(ficha.id), ficha)
        }

        // users/ana/notificacoes/{id}
        val colecaoNotificacoes = FirebaseHelper.usuarioDemo.collection(Constantes.COLECAO_NOTIFICACOES)
        for (notificacao in notificacoes()) {
            lote.set(colecaoNotificacoes.document(notificacao.id), notificacao)
        }

        // users/ana/preferencias/acessibilidade (tudo desligado)
        lote.set(
            FirebaseHelper.usuarioDemo
                .collection(Constantes.COLECAO_PREFERENCIAS)
                .document(Constantes.DOC_PREFERENCIAS),
            Preferencias()
        )

        lote.commit()
            .addOnSuccessListener {
                Log.d(Constantes.TAG_FIREBASE, "Seed concluído: 3 fichas e 5 notificações")
                onSuccess()
            }
            .addOnFailureListener { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Falha no seed", erro)
                onError(erro)
            }
    }

    // Monta a lista de exercícios já com a ordem (1, 2, 3...)
    private fun exercicios(vararg itens: ItemExercicio): List<ItemExercicio> =
        itens.mapIndexed { indice, item -> item.copy(ordem = indice + 1) }

    private fun item(id: String, nome: String, series: Int, repeticoes: Int, cargaKg: Double) =
        ItemExercicio(exercicioId = id, nome = nome, series = series, repeticoes = repeticoes, cargaKg = cargaKg)

    private fun fichas(): List<Ficha> = listOf(
        Ficha(
            id = FICHA_A_ID,
            alunoId = Constantes.USUARIO_DEMO_ID,
            nome = "Treino A — Peito e Tríceps",
            diasSemana = listOf(1, 3), // segunda e quarta
            duracaoMin = 45,
            dificuldade = Constantes.DIFICULDADE_INTERMEDIARIO,
            exercicios = exercicios(
                item("supino_reto", "Supino reto com barra", 4, 10, 60.0),
                item("supino_inclinado", "Supino inclinado c/ halteres", 3, 12, 22.0),
                item("crucifixo_maquina", "Crucifixo na máquina", 3, 12, 35.0),
                item("triceps_polia", "Tríceps na polia", 4, 12, 25.0),
                item("triceps_frances", "Tríceps francês", 3, 10, 14.0),
                item("crossover", "Crossover", 3, 15, 15.0),
                item("paralelas", "Paralelas", 3, 10, 0.0),
                item("triceps_coice", "Tríceps coice", 3, 12, 8.0)
            )
        ),
        Ficha(
            id = FICHA_B_ID,
            alunoId = Constantes.USUARIO_DEMO_ID,
            nome = "Treino B — Costas e Bíceps",
            diasSemana = listOf(2, 4), // terça e quinta
            duracaoMin = 40,
            dificuldade = Constantes.DIFICULDADE_INTERMEDIARIO,
            exercicios = exercicios(
                item("puxada_frontal", "Puxada frontal", 4, 10, 50.0),
                item("remada_curvada", "Remada curvada", 4, 10, 40.0),
                item("remada_baixa", "Remada baixa", 3, 12, 45.0),
                item("pulldown", "Pulldown", 3, 12, 30.0),
                item("rosca_direta", "Rosca direta", 4, 10, 20.0),
                item("rosca_martelo", "Rosca martelo", 3, 12, 12.0),
                item("rosca_concentrada", "Rosca concentrada", 3, 12, 10.0)
            )
        ),
        Ficha(
            id = FICHA_C_ID,
            alunoId = Constantes.USUARIO_DEMO_ID,
            nome = "Treino C — Pernas e Ombros",
            diasSemana = listOf(5), // sexta
            duracaoMin = 55,
            dificuldade = Constantes.DIFICULDADE_AVANCADO,
            exercicios = exercicios(
                item("agachamento_livre", "Agachamento livre", 4, 10, 70.0),
                item("leg_press", "Leg press", 4, 12, 150.0),
                item("cadeira_extensora", "Cadeira extensora", 3, 12, 40.0),
                item("mesa_flexora", "Mesa flexora", 3, 12, 35.0),
                item("panturrilha_em_pe", "Panturrilha em pé", 4, 15, 60.0),
                item("desenvolvimento_halteres", "Desenvolvimento c/ halteres", 4, 10, 16.0),
                item("elevacao_lateral", "Elevação lateral", 3, 12, 8.0),
                item("elevacao_frontal", "Elevação frontal", 3, 12, 8.0),
                item("encolhimento", "Encolhimento", 3, 15, 24.0)
            )
        )
    )

    private fun notificacoes(): List<Notificacao> {
        val fuso = ZoneId.systemDefault()
        val agora = System.currentTimeMillis()
        val inicioDeHoje = LocalDate.now().atStartOfDay(fuso).toInstant().toEpochMilli()

        // "Hoje, há X minutos", sem passar da meia-noite (se o seed rodar de madrugada)
        fun hojeHa(minutos: Long) = Timestamp(Date(maxOf(inicioDeHoje, agora - minutos * 60_000)))

        fun ontemAs(hora: Int, minuto: Int) = Timestamp(
            Date(LocalDate.now().minusDays(1).atTime(hora, minuto).atZone(fuso).toInstant().toEpochMilli())
        )

        return listOf(
            Notificacao("n1", Constantes.CATEGORIA_AULA_CONFIRMADA, "Aula confirmada",
                "Spinning hoje às 18:30", lida = false, criadoEm = hojeHa(10)),
            Notificacao("n2", Constantes.CATEGORIA_HIDRATACAO, "Hora de se hidratar",
                "1,6 L de 2,5 L hoje", lida = false, criadoEm = hojeHa(60)),
            Notificacao("n3", Constantes.CATEGORIA_FICHA_ATUALIZADA, "Nova ficha disponível",
                "Prof. Rafael atualizou seu treino", lida = false, criadoEm = hojeHa(180)),
            Notificacao("n4", Constantes.CATEGORIA_CONQUISTA, "Conquista desbloqueada",
                "5 treinos na semana", lida = true, criadoEm = ontemAs(19, 0)),
            Notificacao("n5", Constantes.CATEGORIA_AULA_CANCELADA, "Aula cancelada",
                "Pilates das 20:30 foi cancelada", lida = true, criadoEm = ontemAs(15, 0))
        )
    }
}
