package com.example.unifit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.DayOfWeek
import java.time.LocalDate

class TreinosActivity : TelaBase() {
    lateinit var layoutDias: LinearLayout
    lateinit var layoutFichas: LinearLayout
    lateinit var txtFichasAtivas: TextView
    lateinit var txtSemFichas: TextView
    lateinit var btnNovaFicha: TextView
    lateinit var btnNotificacoes: ImageView
    lateinit var bottomNavigation: BottomNavigationView

    val siglasDias = listOf("SEG", "TER", "QUA", "QUI", "SEX")
    val nomesDias = listOf("Seg", "Ter", "Qua", "Qui", "Sex")

    lateinit var datasSemana: List<LocalDate>
    var diaHoje = -1
    var diaSelecionado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_treinos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // CONSUMED para o menu inferior não somar o espaço da barra de navegação de novo
            WindowInsetsCompat.CONSUMED
        }

        layoutDias = findViewById(R.id.layoutDias)
        layoutFichas = findViewById(R.id.layoutFichas)
        txtFichasAtivas = findViewById(R.id.txtFichasAtivas)
        txtSemFichas = findViewById(R.id.txtSemFichas)
        btnNovaFicha = findViewById(R.id.btnNovaFicha)
        btnNotificacoes = findViewById(R.id.btnNotificacoes)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        btnNovaFicha.setOnClickListener {
            mostrarDialogoFicha(this, null) { mostrarFichas() }
        }
        btnNotificacoes.setOnClickListener {
            startActivity(Intent(this, NotificacoesActivity::class.java))
        }
        // Atalhos para as telas da aba Treinos (Meu Progresso e Histórico Fitness)
        findViewById<View>(R.id.atalhoProgresso).setOnClickListener { abrirTela(this, ProgressoActivity::class.java) }
        findViewById<View>(R.id.atalhoHistorico).setOnClickListener { abrirTela(this, HistoricoActivity::class.java) }
        configurarMenuInferior(this, bottomNavigation, R.id.nav_treinos)

        calcularSemana()
        mostrarDias()
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a lista ao voltar do detalhe (a ficha pode ter sido editada ou excluída)
        mostrarFichas()
    }

    fun calcularSemana() {
        // Pega de segunda a sexta da semana atual
        val hoje = LocalDate.now()
        val segunda = hoje.with(DayOfWeek.MONDAY)
        datasSemana = List(5) { segunda.plusDays(it.toLong()) }

        // Se hoje for sábado ou domingo, começa selecionando a segunda
        diaHoje = datasSemana.indexOf(hoje)
        diaSelecionado = if (diaHoje >= 0) diaHoje else 0
    }

    fun mostrarDias() {
        layoutDias.removeAllViews()

        for (i in siglasDias.indices) {
            val itemDia = layoutInflater.inflate(R.layout.item_dia, layoutDias, false)
            val txtNomeDia = itemDia.findViewById<TextView>(R.id.txtNomeDia)
            val txtNumeroDia = itemDia.findViewById<TextView>(R.id.txtNumeroDia)

            txtNomeDia.text = siglasDias[i]
            txtNumeroDia.text = datasSemana[i].dayOfMonth.toString()

            if (i == diaSelecionado) {
                itemDia.setBackgroundResource(R.drawable.bg_dia_selecionado)
                txtNomeDia.setTextColor(getColor(R.color.white))
                txtNumeroDia.setTextColor(getColor(R.color.white))
            }

            itemDia.setOnClickListener {
                diaSelecionado = i
                mostrarDias()
                mostrarFichas()
            }

            layoutDias.addView(itemDia)
        }
    }

    fun mostrarFichas() {
        txtFichasAtivas.text = "Fichas Ativas • ${nomesDias[diaSelecionado]}"
        layoutFichas.removeAllViews()
        txtSemFichas.visibility = if (listaFichas.isEmpty()) View.VISIBLE else View.GONE

        for (i in listaFichas.indices) {
            val ficha = listaFichas[i]
            val itemFicha = layoutInflater.inflate(R.layout.item_ficha, layoutFichas, false)

            itemFicha.findViewById<ImageView>(R.id.imgFicha).setImageResource(ficha.imagem)
            itemFicha.findViewById<TextView>(R.id.txtNomeFicha).text = ficha.nome
            itemFicha.findViewById<TextView>(R.id.txtInfoFicha).text =
                "${ficha.exercicios.size} exercícios • ~${ficha.duracaoMin} min"

            // Etiqueta "Hoje" só na primeira ficha e quando o dia de hoje está selecionado
            val mostrarHoje = i == 0 && diaSelecionado == diaHoje
            itemFicha.findViewById<TextView>(R.id.txtHoje).visibility =
                if (mostrarHoje) View.VISIBLE else View.GONE

            itemFicha.setOnClickListener { abrirDetalhe(i) }

            layoutFichas.addView(itemFicha)
        }
    }

    fun abrirDetalhe(indiceFicha: Int) {
        val intent = Intent(this, DetalheTreinoActivity::class.java)
        intent.putExtra("indiceFicha", indiceFicha)
        startActivity(intent)
    }
}
