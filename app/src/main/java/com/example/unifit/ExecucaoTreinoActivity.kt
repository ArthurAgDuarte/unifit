package com.example.unifit

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.time.LocalDate

class ExecucaoTreinoActivity : TelaBase() {
    lateinit var btnFechar: ImageView
    lateinit var txtNomeFicha: TextView
    lateinit var progressoTreino: LinearProgressIndicator
    lateinit var txtProgresso: TextView
    lateinit var txtNomeExercicio: TextView
    lateinit var txtSerie: TextView
    lateinit var txtRepeticoesCarga: TextView
    lateinit var layoutDescanso: LinearLayout
    lateinit var txtTempoDescanso: TextView
    lateinit var btnPularDescanso: TextView
    lateinit var btnConcluirSerie: MaterialButton

    // Descanso entre as séries (a dica da tela de treinos fala em 45 a 60 s)
    val segundosDescanso = 60

    lateinit var ficha: Ficha
    var exercicioAtual = 0
    var serieAtual = 1
    var seriesConcluidas = 0
    var totalSeries = 0
    var inicioTreino = 0L
    var timerDescanso: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_execucao_treino)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnFechar = findViewById(R.id.btnFechar)
        txtNomeFicha = findViewById(R.id.txtNomeFicha)
        progressoTreino = findViewById(R.id.progressoTreino)
        txtProgresso = findViewById(R.id.txtProgresso)
        txtNomeExercicio = findViewById(R.id.txtNomeExercicio)
        txtSerie = findViewById(R.id.txtSerie)
        txtRepeticoesCarga = findViewById(R.id.txtRepeticoesCarga)
        layoutDescanso = findViewById(R.id.layoutDescanso)
        txtTempoDescanso = findViewById(R.id.txtTempoDescanso)
        btnPularDescanso = findViewById(R.id.btnPularDescanso)
        btnConcluirSerie = findViewById(R.id.btnConcluirSerie)

        ficha = listaFichas[intent.getIntExtra("indiceFicha", 0)]
        totalSeries = ficha.exercicios.sumOf { it.series }
        inicioTreino = System.currentTimeMillis()
        txtNomeFicha.text = ficha.nome

        btnConcluirSerie.setOnClickListener { concluirSerie() }
        btnPularDescanso.setOnClickListener { terminarDescanso() }
        btnFechar.setOnClickListener { confirmarEncerrar() }

        // O botão "voltar" do celular também pergunta antes de sair
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmarEncerrar()
            }
        })

        mostrarExercicio()
    }

    fun mostrarExercicio() {
        val exercicio = ficha.exercicios[exercicioAtual]

        txtProgresso.text = "Exercício ${exercicioAtual + 1} de ${ficha.exercicios.size}"
        progressoTreino.progress = seriesConcluidas * 100 / totalSeries
        txtNomeExercicio.text = exercicio.nome
        txtSerie.text = "Série $serieAtual de ${exercicio.series}"
        txtRepeticoesCarga.text = "${exercicio.repeticoes} repetições • ${exercicio.carga}"
    }

    fun concluirSerie() {
        seriesConcluidas++
        val exercicio = ficha.exercicios[exercicioAtual]

        if (serieAtual < exercicio.series) {
            // Próxima série do mesmo exercício
            serieAtual++
        } else if (exercicioAtual < ficha.exercicios.size - 1) {
            // Próximo exercício
            exercicioAtual++
            serieAtual = 1
        } else {
            // Era a última série do último exercício
            finalizarTreino()
            return
        }

        mostrarExercicio()
        iniciarDescanso()
    }

    fun iniciarDescanso() {
        layoutDescanso.visibility = View.VISIBLE
        btnConcluirSerie.isEnabled = false
        btnConcluirSerie.text = "Descansando..."

        timerDescanso = object : CountDownTimer(segundosDescanso * 1000L, 1000) {
            override fun onTick(millisRestantes: Long) {
                val segundos = (millisRestantes / 1000).toInt() + 1
                txtTempoDescanso.text = String.format("%02d:%02d", segundos / 60, segundos % 60)
            }

            override fun onFinish() {
                terminarDescanso()
            }
        }.start()
    }

    fun terminarDescanso() {
        timerDescanso?.cancel()
        layoutDescanso.visibility = View.GONE
        btnConcluirSerie.isEnabled = true
        btnConcluirSerie.text = "Concluir série"
    }

    fun finalizarTreino() {
        progressoTreino.progress = 100
        val minutos = maxOf(1, ((System.currentTimeMillis() - inicioTreino) / 60000).toInt())
        historicoTreinos.add(TreinoConcluido(ficha.nome, LocalDate.now(), minutos))

        MaterialAlertDialogBuilder(this)
            .setTitle("Treino concluído! 🎉")
            .setMessage("Você fez $totalSeries séries em $minutos min. Mandou bem!")
            .setCancelable(false)
            .setPositiveButton("Finalizar") { _, _ -> finish() }
            .show()
    }

    fun confirmarEncerrar() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Encerrar treino?")
            .setMessage("O progresso deste treino não será salvo.")
            .setNegativeButton("Continuar treinando", null)
            .setPositiveButton("Encerrar") { _, _ -> finish() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerDescanso?.cancel()
    }
}
