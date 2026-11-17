package com.example.unifit

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class OnboardingActivity : TelaBase() {
    lateinit var imgIlustracao: ImageView
    lateinit var txtTituloOnboarding: TextView
    lateinit var txtDescricao: TextView
    lateinit var btnPular: TextView
    lateinit var btnComecar: MaterialButton
    lateinit var indicadores: List<View>

    // Conteúdo de cada página do onboarding
    val icones = listOf(R.drawable.ic_haltere, R.drawable.ic_grafico, R.drawable.ic_calendario)
    val titulos = listOf(
        "Organize seus treinos",
        "Acompanhe sua evolução",
        "Treine no seu ritmo"
    )
    val descricoes = listOf(
        "Monte sua ficha, acompanhe séries, cargas e evolua a cada semana.",
        "Veja seu progresso em gráficos e bata suas metas com constância.",
        "Agende seus horários na academia da universidade e não perca nenhum treino."
    )

    var paginaAtual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgIlustracao = findViewById(R.id.imgIlustracao)
        txtTituloOnboarding = findViewById(R.id.txtTituloOnboarding)
        txtDescricao = findViewById(R.id.txtDescricao)
        btnPular = findViewById(R.id.btnPular)
        btnComecar = findViewById(R.id.btnComecar)
        indicadores = listOf(
            findViewById(R.id.indicador1),
            findViewById(R.id.indicador2),
            findViewById(R.id.indicador3)
        )

        btnComecar.setOnClickListener { proximaPagina() }
        btnPular.setOnClickListener { finalizarOnboarding() }

        mostrarPagina(0)
    }

    fun proximaPagina() {
        if (paginaAtual < titulos.size - 1) {
            mostrarPagina(paginaAtual + 1)
        } else {
            finalizarOnboarding()
        }
    }

    fun mostrarPagina(pagina: Int) {
        paginaAtual = pagina

        imgIlustracao.setImageResource(icones[pagina])
        txtTituloOnboarding.text = titulos[pagina]
        txtDescricao.text = descricoes[pagina]

        // Na última página o botão vira "Começar" e o "Pular" some
        val ultimaPagina = pagina == titulos.size - 1
        btnComecar.text = if (ultimaPagina) "Começar" else "Próximo"
        btnPular.visibility = if (ultimaPagina) View.INVISIBLE else View.VISIBLE

        atualizarIndicadores()
    }

    fun atualizarIndicadores() {
        for (i in indicadores.indices) {
            val indicador = indicadores[i]
            val ativo = i == paginaAtual

            // Página atual: pílula de 24dp | demais: bolinha de 8dp
            indicador.layoutParams.width = dpParaPx(if (ativo) 24 else 8)
            indicador.setBackgroundResource(
                if (ativo) R.drawable.bg_indicador_ativo else R.drawable.bg_indicador_inativo
            )
            indicador.requestLayout()
        }
    }

    fun finalizarOnboarding() {
        // Começar / Pular -> Login (RF02.2 e RF02.3)
        abrirTelaLimpandoHistorico(this, LoginActivity::class.java)
    }

    fun dpParaPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
