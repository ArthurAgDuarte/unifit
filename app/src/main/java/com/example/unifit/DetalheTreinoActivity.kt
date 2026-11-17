package com.example.unifit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetalheTreinoActivity : TelaBase() {
    lateinit var btnVoltar: ImageView
    lateinit var btnMaisOpcoes: ImageView
    lateinit var txtNomeTreino: TextView
    lateinit var txtQtdExercicios: TextView
    lateinit var txtDuracao: TextView
    lateinit var txtNivel: TextView
    lateinit var txtQtdItens: TextView
    lateinit var txtSemExercicios: TextView
    lateinit var layoutExercicios: LinearLayout
    lateinit var btnIniciarTreino: MaterialButton

    var indiceFicha = 0
    lateinit var ficha: Ficha

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_treino)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        btnMaisOpcoes = findViewById(R.id.btnMaisOpcoes)
        txtNomeTreino = findViewById(R.id.txtNomeTreino)
        txtQtdExercicios = findViewById(R.id.txtQtdExercicios)
        txtDuracao = findViewById(R.id.txtDuracao)
        txtNivel = findViewById(R.id.txtNivel)
        txtQtdItens = findViewById(R.id.txtQtdItens)
        txtSemExercicios = findViewById(R.id.txtSemExercicios)
        layoutExercicios = findViewById(R.id.layoutExercicios)
        btnIniciarTreino = findViewById(R.id.btnIniciarTreino)

        // Recebe qual ficha foi clicada na tela Meus Treinos
        indiceFicha = intent.getIntExtra("indiceFicha", 0)
        ficha = listaFichas[indiceFicha]

        btnVoltar.setOnClickListener { finish() }
        btnMaisOpcoes.setOnClickListener { mostrarMaisOpcoes() }
        btnIniciarTreino.setOnClickListener { iniciarTreino() }

        mostrarResumo()
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a lista ao voltar do Detalhe do Exercício (exercício concluído)
        mostrarExercicios()
    }

    fun mostrarResumo() {
        val quantidade = ficha.exercicios.size
        txtNomeTreino.text = ficha.nome
        txtQtdExercicios.text = "$quantidade exercícios"
        txtDuracao.text = "~${ficha.duracaoMin} min"
        txtNivel.text = ficha.nivel
        txtQtdItens.text = "$quantidade itens"
    }

    fun mostrarExercicios() {
        layoutExercicios.removeAllViews()
        txtSemExercicios.visibility = if (ficha.exercicios.isEmpty()) View.VISIBLE else View.GONE

        for (i in ficha.exercicios.indices) {
            val exercicio = ficha.exercicios[i]
            val itemExercicio = layoutInflater.inflate(R.layout.item_exercicio, layoutExercicios, false)

            itemExercicio.findViewById<TextView>(R.id.txtNomeExercicio).text = exercicio.nome
            itemExercicio.findViewById<TextView>(R.id.txtSeriesExercicio).text =
                "${exercicio.series} × ${exercicio.repeticoes} • ${exercicio.carga}" +
                    if (ficha.concluidos.contains(i)) " • ✓ concluído" else ""

            // Tocar no card: Detalhe do Exercício (RF09.5)
            itemExercicio.setOnClickListener {
                val intent = Intent(this, DetalheExercicioActivity::class.java)
                intent.putExtra("indiceFicha", indiceFicha)
                intent.putExtra("indiceExercicio", i)
                startActivity(intent)
            }

            // Play: procura um vídeo de como fazer o exercício no YouTube
            itemExercicio.findViewById<ImageView>(R.id.btnPlay).setOnClickListener {
                val busca = Uri.encode("como fazer ${exercicio.nome}")
                abrirLink(this, "https://www.youtube.com/results?search_query=$busca")
            }

            // Segurar o card: remover o exercício
            itemExercicio.setOnLongClickListener {
                confirmarRemoverExercicio(i)
                true
            }

            layoutExercicios.addView(itemExercicio)
        }
    }

    fun mostrarMaisOpcoes() {
        val menu = PopupMenu(this, btnMaisOpcoes)
        menu.menuInflater.inflate(R.menu.menu_detalhe_treino, menu.menu)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.opcao_adicionar_exercicio -> mostrarDialogoExercicio(this, ficha) { atualizarTela() }
                R.id.opcao_editar_ficha -> mostrarDialogoFicha(this, ficha) { atualizarTela() }
                R.id.opcao_excluir_ficha -> confirmarExcluirFicha()
            }
            true
        }
        menu.show()
    }

    fun atualizarTela() {
        mostrarResumo()
        mostrarExercicios()
    }

    fun confirmarRemoverExercicio(posicao: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remover exercício?")
            .setMessage("\"${ficha.exercicios[posicao].nome}\" será removido desta ficha.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Remover") { _, _ ->
                ficha.exercicios.removeAt(posicao)
                atualizarTela()
            }
            .show()
    }

    fun confirmarExcluirFicha() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Excluir ficha?")
            .setMessage("A ficha \"${ficha.nome}\" e todos os exercícios dela serão apagados.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                listaFichas.removeAt(indiceFicha)
                Toast.makeText(this, "Ficha excluída", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }

    fun iniciarTreino() {
        if (ficha.exercicios.isEmpty()) {
            Toast.makeText(this, "Adicione exercícios antes de iniciar", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, ExecucaoTreinoActivity::class.java)
        intent.putExtra("indiceFicha", indiceFicha)
        startActivity(intent)
    }
}
