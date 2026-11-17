package com.example.unifit

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

// Tela "Exercício" (RF10). Abre de dois jeitos:
// - pela ficha (Detalhe do Treino): extras "indiceFicha" e "indiceExercicio"
// - pelo catálogo do admin (Exercícios e Aparelhos): extra "nomeExercicio"
class DetalheExercicioActivity : TelaBase() {
    lateinit var btnVoltar: View
    lateinit var frameVideo: FrameLayout
    lateinit var txtDuracaoVideo: TextView
    lateinit var txtNomeExercicio: TextView
    lateinit var txtMusculos: TextView
    lateinit var txtSeries: TextView
    lateinit var txtRepeticoes: TextView
    lateinit var txtCarga: TextView
    lateinit var layoutPassos: LinearLayout
    lateinit var btnVerEmLibras: MaterialButton
    lateinit var btnConcluirExercicio: MaterialButton
    lateinit var divisorRodape: View

    var ficha: Ficha? = null
    var indiceExercicio = -1
    var nomeExercicio = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_exercicio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        frameVideo = findViewById(R.id.frameVideo)
        txtDuracaoVideo = findViewById(R.id.txtDuracaoVideo)
        txtNomeExercicio = findViewById(R.id.txtNomeExercicio)
        txtMusculos = findViewById(R.id.txtMusculos)
        txtSeries = findViewById(R.id.txtSeries)
        txtRepeticoes = findViewById(R.id.txtRepeticoes)
        txtCarga = findViewById(R.id.txtCarga)
        layoutPassos = findViewById(R.id.layoutPassos)
        btnVerEmLibras = findViewById(R.id.btnVerEmLibras)
        btnConcluirExercicio = findViewById(R.id.btnConcluirExercicio)
        divisorRodape = findViewById(R.id.divisorRodape)

        val indiceFicha = intent.getIntExtra("indiceFicha", -1)
        indiceExercicio = intent.getIntExtra("indiceExercicio", -1)

        if (indiceFicha >= 0 && indiceExercicio >= 0) {
            // Veio da ficha: mostra séries, repetições e carga
            val f = listaFichas[indiceFicha]
            val exercicio = f.exercicios[indiceExercicio]
            ficha = f
            nomeExercicio = exercicio.nome
            txtSeries.text = exercicio.series.toString()
            txtRepeticoes.text = exercicio.repeticoes.toString()
            txtCarga.text = exercicio.carga
        } else {
            // Veio do catálogo: não tem séries/carga e não dá para concluir
            nomeExercicio = intent.getStringExtra("nomeExercicio") ?: "Exercício"
            txtSeries.text = "—"
            txtRepeticoes.text = "—"
            txtCarga.text = "—"
            btnConcluirExercicio.visibility = View.GONE
            divisorRodape.visibility = View.GONE
        }

        mostrarInformacoes()
        atualizarBotaoConcluir()

        btnVoltar.setOnClickListener { finish() }
        frameVideo.setOnClickListener { abrirVideo() }
        btnVerEmLibras.setOnClickListener {
            // TODO: integrar o VLibras para traduzir o passo a passo
            Toast.makeText(this, "Tradução em Libras (VLibras): em breve", Toast.LENGTH_SHORT).show()
        }
        btnConcluirExercicio.setOnClickListener { concluirExercicio() }
    }

    fun mostrarInformacoes() {
        val info = infoDe(nomeExercicio)
        txtNomeExercicio.text = nomeExercicio
        txtMusculos.text = intent.getStringExtra("musculos") ?: info.musculos
        txtDuracaoVideo.text = info.duracaoVideo

        layoutPassos.removeAllViews()
        for (i in info.passos.indices) {
            val itemPasso = layoutInflater.inflate(R.layout.item_passo, layoutPassos, false)
            itemPasso.findViewById<TextView>(R.id.txtNumeroPasso).text = (i + 1).toString()
            itemPasso.findViewById<TextView>(R.id.txtPasso).text = info.passos[i]
            layoutPassos.addView(itemPasso)
        }
    }

    fun abrirVideo() {
        val busca = Uri.encode("como fazer $nomeExercicio")
        abrirLink(this, "https://www.youtube.com/results?search_query=$busca")
    }

    fun atualizarBotaoConcluir() {
        val f = ficha ?: return
        if (f.concluidos.contains(indiceExercicio)) {
            btnConcluirExercicio.text = "Exercício concluído ✓"
            btnConcluirExercicio.isEnabled = false
        }
    }

    // Marca como concluído e atualiza o progresso do treino do dia (RF10.3)
    fun concluirExercicio() {
        val f = ficha ?: return
        f.concluidos.add(indiceExercicio)
        Toast.makeText(this, "Concluído! ${f.concluidos.size} de ${f.exercicios.size} exercícios", Toast.LENGTH_SHORT).show()
        finish()
    }
}
