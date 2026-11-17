package com.example.unifit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Tela "Histórico Fitness" (RF16, pág. 35)
class HistoricoActivity : TelaBase() {
    lateinit var layoutPeriodos: LinearLayout
    lateinit var btnCalendario: ImageView
    lateinit var layoutHistorico: LinearLayout
    lateinit var txtSemAtividades: TextView
    lateinit var btnNotificacoes: View
    lateinit var bottomNavigation: BottomNavigationView

    val periodos = listOf("7 dias", "30 dias", "3 meses")
    val diasDoPeriodo = listOf(7, 30, 90)
    var periodoSelecionado = 0
    var diaEscolhido: Int? = null // dias atrás escolhidos no calendário (null = usa o período)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historico)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        layoutPeriodos = findViewById(R.id.layoutPeriodos)
        btnCalendario = findViewById(R.id.btnCalendario)
        layoutHistorico = findViewById(R.id.layoutHistorico)
        txtSemAtividades = findViewById(R.id.txtSemAtividades)
        btnNotificacoes = findViewById(R.id.btnNotificacoes)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Esta tela fica "dentro" da aba Treinos (como no protótipo)
        configurarMenuInferior(this, bottomNavigation, R.id.nav_treinos, false)
        btnNotificacoes.setOnClickListener { abrirTela(this, NotificacoesActivity::class.java) }
        btnCalendario.setOnClickListener { escolherData() }

        mostrarPeriodos()
        mostrarHistorico()
    }

    fun mostrarPeriodos() {
        // Com um dia escolhido no calendário, nenhum chip fica marcado
        val marcado = if (diaEscolhido == null) periodoSelecionado else -1
        montarChips(this, layoutPeriodos, periodos, marcado) { escolhido ->
            periodoSelecionado = escolhido
            diaEscolhido = null
            mostrarPeriodos()
            mostrarHistorico()
        }
    }

    // Atividades de exemplo + treinos concluídos no app (Execução do treino)
    fun todasAtividades(): List<AtividadeHistorico> {
        val hoje = LocalDate.now()
        val concluidosNoApp = historicoTreinos.map {
            AtividadeHistorico(it.nomeFicha, ChronoUnit.DAYS.between(it.data, hoje).toInt(), it.minutos,
                "concluído no app", R.drawable.ic_haltere)
        }
        return concluidosNoApp + historicoAtividades
    }

    fun mostrarHistorico() {
        layoutHistorico.removeAllViews()
        val filtradas = todasAtividades().filter {
            val escolhido = diaEscolhido
            if (escolhido != null) it.diasAtras == escolhido else it.diasAtras < diasDoPeriodo[periodoSelecionado]
        }
        txtSemAtividades.visibility = if (filtradas.isEmpty()) View.VISIBLE else View.GONE

        // Agrupa por dia (RF16.1)
        for (diasAtras in filtradas.map { it.diasAtras }.distinct().sorted()) {
            val itemDia = layoutInflater.inflate(R.layout.item_dia_historico, layoutHistorico, false)
            itemDia.findViewById<TextView>(R.id.txtDiaHistorico).text = tituloDoDia(diasAtras)
            if (diasAtras == 0) itemDia.findViewById<View>(R.id.pontoDia).setBackgroundResource(R.drawable.bg_ponto_azul)

            val layoutAtividadesDia = itemDia.findViewById<LinearLayout>(R.id.layoutAtividadesDia)
            for (atividade in filtradas.filter { it.diasAtras == diasAtras }) {
                val card = layoutInflater.inflate(R.layout.item_card_lista, layoutAtividadesDia, false)
                card.findViewById<ImageView>(R.id.imgItem).setImageResource(atividade.icone)
                card.findViewById<TextView>(R.id.txtTituloItem).text = atividade.titulo
                card.findViewById<TextView>(R.id.txtSubtituloItem).text = "${atividade.minutos} min • ${atividade.info}"
                card.setOnClickListener { mostrarDetalhes(atividade) }
                layoutAtividadesDia.addView(card)
            }
            layoutHistorico.addView(itemDia)
        }
    }

    // "Hoje • 16 set", "Ontem • 15 set", "Seg • 14 set"
    fun tituloDoDia(diasAtras: Int): String {
        val data = LocalDate.now().minusDays(diasAtras.toLong())
        val nome = when (diasAtras) {
            0 -> "Hoje"
            1 -> "Ontem"
            else -> diasDaSemana[data.dayOfWeek.value - 1]
        }
        return "$nome • ${dataCurta(data)}"
    }

    // Detalhes de uma atividade (RF16.2)
    fun mostrarDetalhes(atividade: AtividadeHistorico) {
        MaterialAlertDialogBuilder(this)
            .setTitle(atividade.titulo)
            .setMessage("Data: ${tituloDoDia(atividade.diasAtras)}\nDuração: ${atividade.minutos} min\n${atividade.info.replaceFirstChar { it.uppercase() }}")
            .setPositiveButton("Fechar", null)
            .show()
    }

    // Ícone de calendário: mostra só as atividades do dia escolhido
    fun escolherData() {
        val hoje = LocalDate.now()
        val seletor = DatePickerDialog(this, { _, ano, mes, dia ->
            val escolhida = LocalDate.of(ano, mes + 1, dia)
            diaEscolhido = ChronoUnit.DAYS.between(escolhida, hoje).toInt()
            mostrarPeriodos()
            mostrarHistorico()
        }, hoje.year, hoje.monthValue - 1, hoje.dayOfMonth)
        seletor.datePicker.maxDate = System.currentTimeMillis()
        seletor.show()
    }
}
