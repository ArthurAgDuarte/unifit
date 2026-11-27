package com.example.unifit.ui.detalhetreino

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unifit.DetalheExercicioActivity
import com.example.unifit.R
import com.example.unifit.data.FichaRepository
import com.example.unifit.data.ProgressoRepository
import com.example.unifit.databinding.ActivityDetalheTreinoBinding
import com.example.unifit.model.Ficha
import com.example.unifit.model.ItemExercicio
import com.example.unifit.model.Progresso
import com.example.unifit.ui.base.BaseActivity
import com.example.unifit.util.Constantes
import com.example.unifit.util.Formatos
import com.example.unifit.util.Mensagens
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp

// Detalhe de uma ficha: resumo, exercícios (com os concluídos hoje) e "Iniciar treino"
class DetalheTreinoActivity : BaseActivity() {

    private lateinit var binding: ActivityDetalheTreinoBinding
    private val fichaRepository = FichaRepository()
    private val progressoRepository = ProgressoRepository()
    private lateinit var exercicioAdapter: ExercicioAdapter

    private lateinit var fichaId: String
    private var ficha: Ficha? = null
    private var progressoDeHoje: Progresso? = null

    private val launcherExercicio = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val idConcluido = result.data?.getStringExtra("exercicioConcluidoId")
            if (!idConcluido.isNullOrEmpty()) {
                val listaConcluidos = progressoDeHoje?.exerciciosConcluidos.orEmpty().toMutableList()
                if (!listaConcluidos.contains(idConcluido)) {
                    listaConcluidos.add(idConcluido)
                }
                progressoDeHoje = (progressoDeHoje ?: Progresso()).copy(
                    exerciciosConcluidos = listaConcluidos
                )
                mostrarExercicios()
            } else {
                carregarProgresso()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sem fichaId não há o que mostrar: fecha com uma mensagem (Toast porque a tela vai fechar)
        val idRecebido = intent.getStringExtra(Constantes.EXTRA_FICHA_ID)
        if (idRecebido.isNullOrBlank()) {
            Toast.makeText(this, R.string.ficha_invalida, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        fichaId = idRecebido

        binding = ActivityDetalheTreinoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        exercicioAdapter = ExercicioAdapter(
            aoTocar = { itemExercicio -> abrirDetalheExercicio(itemExercicio) },
            aoTocarPlay = { itemExercicio -> abrirDetalheExercicio(itemExercicio) }
        )
        binding.rvExercicios.layoutManager = LinearLayoutManager(this)
        binding.rvExercicios.adapter = exercicioAdapter

        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnMaisOpcoes.setOnClickListener { mostrarMensagem(R.string.em_breve) }
        binding.btnIniciarTreino.setOnClickListener { aoTocarIniciar() }
        binding.btnIniciarTreino.isEnabled = false // libera depois que a ficha carregar

        carregarFicha()
    }

    override fun onResume() {
        super.onResume()
        if (this::fichaId.isInitialized && ficha != null) {
            carregarProgresso()
        }
    }

    private fun abrirDetalheExercicio(item: ItemExercicio) {
        val intent = Intent(this, DetalheExercicioActivity::class.java).apply {
            putExtra(Constantes.EXTRA_FICHA_ID, fichaId)
            putExtra("exercicioId", item.exercicioId)
            putExtra("nomeExercicio", item.nome)
            putExtra("series", item.series)
            putExtra("repeticoes", item.repeticoes)
            putExtra("carga", Formatos.carga(this@DetalheTreinoActivity, item.cargaKg))
        }
        launcherExercicio.launch(intent)
    }

    // 1) ficha  2) progresso de hoje (para marcar os concluídos e o estado do botão)
    private fun carregarFicha() {
        binding.progressExercicios.visibility = View.VISIBLE
        binding.rvExercicios.visibility = View.GONE
        binding.txtSemExercicios.visibility = View.GONE

        fichaRepository.buscarFicha(
            fichaId,
            onSuccess = { resultado ->
                if (isFinishing || isDestroyed) return@buscarFicha
                if (resultado == null) {
                    Toast.makeText(this, R.string.ficha_nao_encontrada, Toast.LENGTH_SHORT).show()
                    finish()
                    return@buscarFicha
                }
                ficha = resultado
                mostrarResumo(resultado)
                carregarProgresso()
            },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao carregar a ficha $fichaId", erro)
                if (isFinishing || isDestroyed) return@buscarFicha
                binding.progressExercicios.visibility = View.GONE
                Snackbar.make(binding.root, R.string.erro_carregar_ficha, Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(binding.btnIniciarTreino)
                    .setAction(R.string.tentar_novamente) { carregarFicha() }
                    .show()
            }
        )
    }

    private fun carregarProgresso() {
        progressoRepository.buscarProgressoDeHoje(
            onSuccess = { progresso ->
                if (isFinishing || isDestroyed) return@buscarProgressoDeHoje
                progressoDeHoje = progresso
                mostrarExercicios()
            },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao carregar o progresso de hoje", erro)
                if (isFinishing || isDestroyed) return@buscarProgressoDeHoje
                // A ficha já carregou: mostra a lista mesmo sem as marcações
                mostrarExercicios()
                mostrarMensagem(R.string.erro_carregar_progresso)
            }
        )
    }

    private fun mostrarResumo(ficha: Ficha) {
        val quantidade = ficha.exercicios.size
        binding.txtNomeTreino.text = ficha.nome
        binding.txtQtdExercicios.text =
            resources.getQuantityString(R.plurals.ficha_qtd_exercicios, quantidade, quantidade)
        binding.txtDuracao.text = getString(R.string.ficha_duracao, ficha.duracaoMin)
        binding.txtNivel.text = ficha.dificuldade
        binding.txtQtdItens.text = resources.getQuantityString(R.plurals.ficha_qtd_itens, quantidade, quantidade)
    }

    private fun mostrarExercicios() {
        val fichaAtual = ficha ?: return
        val concluidos = progressoDeHoje?.exerciciosConcluidos.orEmpty().toSet()

        // Filtra para remover os exercícios concluídos da exibição na tela
        val exerciciosPendentes = fichaAtual.exercicios.filter { it.exercicioId !in concluidos }
        val linhas = exerciciosPendentes.map { ExercicioLinha(it, false) }
        exercicioAdapter.submitList(linhas)

        binding.progressExercicios.visibility = View.GONE
        binding.rvExercicios.visibility = if (linhas.isEmpty()) View.GONE else View.VISIBLE
        binding.txtSemExercicios.visibility = if (linhas.isEmpty()) View.VISIBLE else View.GONE

        atualizarBotao()
    }

    // "Treino em andamento" quando o progresso de hoje já é desta ficha e foi iniciado
    private fun treinoEmAndamento(): Boolean {
        val progresso = progressoDeHoje ?: return false
        return progresso.fichaId == fichaId && progresso.iniciadoEm != null
    }

    private fun atualizarBotao() {
        binding.btnIniciarTreino.isEnabled = true
        binding.btnIniciarTreino.setText(
            if (treinoEmAndamento()) R.string.treino_em_andamento else R.string.iniciar_treino
        )
    }

    private fun aoTocarIniciar() {
        if (treinoEmAndamento()) {
            // A tela de execução do treino fica fora do escopo
            mostrarMensagem(R.string.em_breve)
            return
        }

        binding.btnIniciarTreino.isEnabled = false // evita gravar duas vezes com toque duplo
        progressoRepository.iniciarTreino(
            fichaId,
            onSuccess = {
                if (isFinishing || isDestroyed) return@iniciarTreino
                // Atualiza o estado local sem precisar ler de novo
                // (o horário real fica no servidor; aqui só importa que não é nulo)
                progressoDeHoje = (progressoDeHoje ?: Progresso()).copy(
                    fichaId = fichaId,
                    iniciadoEm = Timestamp.now()
                )
                atualizarBotao()
                mostrarMensagem(R.string.treino_iniciado)
            },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao iniciar o treino", erro)
                if (isFinishing || isDestroyed) return@iniciarTreino
                binding.btnIniciarTreino.isEnabled = true
                mostrarMensagem(R.string.erro_iniciar_treino)
            }
        )
    }

    // Snackbar acima do botão "Iniciar treino"
    private fun mostrarMensagem(@StringRes mensagem: Int) {
        Mensagens.mostrar(binding.root, mensagem, binding.btnIniciarTreino)
    }
}