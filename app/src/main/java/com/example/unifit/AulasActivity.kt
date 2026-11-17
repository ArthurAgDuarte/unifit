package com.example.unifit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate

// Tela "Aulas Coletivas" (RF12, pág. 31)
class AulasActivity : TelaBase() {
    lateinit var layoutCategorias: LinearLayout
    lateinit var layoutAulas: LinearLayout
    lateinit var txtSemAulas: TextView
    lateinit var btnNotificacoes: View
    lateinit var bottomNavigation: BottomNavigationView

    var categoriaSelecionada = 0
    var verSemana = false // RF12.1: "Ver semana" mostra os próximos 7 dias

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aulas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        layoutCategorias = findViewById(R.id.layoutCategorias)
        layoutAulas = findViewById(R.id.layoutAulas)
        txtSemAulas = findViewById(R.id.txtSemAulas)
        btnNotificacoes = findViewById(R.id.btnNotificacoes)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        configurarMenuInferior(this, bottomNavigation, R.id.nav_aulas)
        btnNotificacoes.setOnClickListener { abrirTela(this, NotificacoesActivity::class.java) }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza as vagas ao voltar do Detalhe da Aula
        mostrarCategorias()
        mostrarAulas()
    }

    fun mostrarCategorias() {
        montarChips(this, layoutCategorias, categoriasAulas, categoriaSelecionada) { escolhida ->
            categoriaSelecionada = escolhida
            mostrarCategorias()
            mostrarAulas()
        }
    }

    fun mostrarAulas() {
        layoutAulas.removeAllViews()
        val categoria = categoriasAulas[categoriaSelecionada]
        val ultimoDia = if (verSemana) 6 else 1
        var total = 0

        for (dia in 0..ultimoDia) {
            val indicesDoDia = listaAulas.indices.filter {
                listaAulas[it].diaOffset == dia && (categoria == "Todas" || listaAulas[it].categoria == categoria)
            }
            // O "Amanhã" sempre aparece porque é nele que fica o link "Ver semana"
            if (indicesDoDia.isEmpty() && dia != 1) continue

            adicionarSecao(dia)
            for (i in indicesDoDia) {
                val item = layoutInflater.inflate(R.layout.item_card_lista, layoutAulas, false)
                preencherCardAula(this, item, listaAulas[i], false)
                item.setOnClickListener { abrirDetalheAula(i) }
                layoutAulas.addView(item)
                total++
            }
        }
        txtSemAulas.visibility = if (total == 0) View.VISIBLE else View.GONE
    }

    fun adicionarSecao(dia: Int) {
        val secao = layoutInflater.inflate(R.layout.item_secao_aulas, layoutAulas, false)
        val txtTitulo = secao.findViewById<TextView>(R.id.txtTituloSecao)

        if (dia == 0) {
            // Como no protótipo: "Hoje, 16 de setembro" pequeno e cinza
            txtTitulo.text = "Hoje, ${dataPorExtenso(LocalDate.now())}"
            txtTitulo.textSize = 14f
            txtTitulo.setTextColor(getColor(R.color.texto_secundario))
        } else {
            txtTitulo.text = nomeDoDia(dia)
        }

        if (dia == 1) {
            val btnVerSemana = secao.findViewById<TextView>(R.id.btnVerSemana)
            btnVerSemana.visibility = View.VISIBLE
            btnVerSemana.text = if (verSemana) "Ver menos" else "Ver semana"
            btnVerSemana.setOnClickListener {
                verSemana = !verSemana
                mostrarAulas()
            }
        }
        layoutAulas.addView(secao)
    }

    fun abrirDetalheAula(indiceAula: Int) {
        val intent = Intent(this, DetalheAulaActivity::class.java)
        intent.putExtra("indiceAula", indiceAula)
        startActivity(intent)
    }
}
