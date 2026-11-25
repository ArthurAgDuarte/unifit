package com.example.unifit.ui.notificacoes

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unifit.R
import com.example.unifit.data.NotificacaoRepository
import com.example.unifit.databinding.ActivityNotificacoesBinding
import com.example.unifit.model.Notificacao
import com.example.unifit.ui.base.BaseActivity
import com.example.unifit.util.Constantes
import com.example.unifit.util.Datas
import com.example.unifit.util.Mensagens
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration

// Lista de notificações em tempo real (addSnapshotListener), agrupada por dia
class NotificacoesActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificacoesBinding
    private val notificacaoRepository = NotificacaoRepository()
    private lateinit var adapter: NotificacaoAdapter

    // Guardado para remover o listener em onStop
    private var registro: ListenerRegistration? = null
    private var recebeuPrimeiraLista = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        adapter = NotificacaoAdapter { notificacao -> aoTocar(notificacao) }
        binding.rvNotificacoes.adapter = adapter // o LayoutManager está no XML

        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnMarcarTodas.setOnClickListener { marcarTodas() }
        binding.btnMarcarTodas.visibility = View.GONE // aparece quando houver não lidas
    }

    override fun onStart() {
        super.onStart()
        if (!recebeuPrimeiraLista) binding.progressNotificacoes.visibility = View.VISIBLE
        registro = notificacaoRepository.ouvirNotificacoes(
            onChange = { lista -> mostrar(lista) },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro no listener de notificações", erro)
                binding.progressNotificacoes.visibility = View.GONE
                Mensagens.mostrar(binding.root, R.string.erro_carregar_notificacoes)
            }
        )
    }

    override fun onStop() {
        // Para de ouvir o Firestore quando a tela sai de vista
        registro?.remove()
        registro = null
        super.onStop()
    }

    private fun mostrar(notificacoes: List<Notificacao>) {
        recebeuPrimeiraLista = true
        binding.progressNotificacoes.visibility = View.GONE

        adapter.submitList(montarLinhas(notificacoes))
        binding.txtSemNotificacoes.visibility = if (notificacoes.isEmpty()) View.VISIBLE else View.GONE

        // "3 não lidas" + botão "Marcar todas como lidas" só quando há o que marcar
        val naoLidas = notificacoes.count { !it.lida }
        binding.txtNaoLidas.text = if (naoLidas == 0) {
            getString(R.string.nenhuma_nao_lida)
        } else {
            resources.getQuantityString(R.plurals.notificacoes_nao_lidas, naoLidas, naoLidas)
        }
        binding.btnMarcarTodas.visibility = if (naoLidas > 0) View.VISIBLE else View.GONE
    }

    // A lista já chega das mais novas para as mais antigas: insere um cabeçalho sempre que o dia muda
    private fun montarLinhas(notificacoes: List<Notificacao>): List<LinhaNotificacao> {
        val linhas = mutableListOf<LinhaNotificacao>()
        var rotuloAnterior: String? = null
        for (notificacao in notificacoes) {
            // criadoEm pode estar nulo por um instante numa gravação com serverTimestamp
            val momento = notificacao.criadoEm ?: Timestamp.now()
            val rotulo = Datas.rotuloDoDia(this, Datas.dataLocal(momento))
            if (rotulo != rotuloAnterior) {
                linhas.add(LinhaNotificacao.Cabecalho(rotulo))
                rotuloAnterior = rotulo
            }
            linhas.add(LinhaNotificacao.Item(notificacao, Datas.tempoRelativo(this, momento)))
        }
        return linhas
    }

    // Tocar marca só esta como lida; o listener atualiza a lista sozinho
    private fun aoTocar(notificacao: Notificacao) {
        if (notificacao.lida) return
        notificacaoRepository.marcarComoLida(
            notificacao.id,
            onSuccess = { },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao marcar ${notificacao.id} como lida", erro)
                if (!isFinishing && !isDestroyed) Mensagens.mostrar(binding.root, R.string.erro_marcar_lida)
            }
        )
    }

    // WriteBatch com todas as não lidas (feito no repositório)
    private fun marcarTodas() {
        binding.btnMarcarTodas.isEnabled = false
        notificacaoRepository.marcarTodasComoLidas(
            onSuccess = {
                if (!isFinishing && !isDestroyed) binding.btnMarcarTodas.isEnabled = true
            },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao marcar todas como lidas", erro)
                if (isFinishing || isDestroyed) return@marcarTodasComoLidas
                binding.btnMarcarTodas.isEnabled = true
                Mensagens.mostrar(binding.root, R.string.erro_marcar_lida)
            }
        )
    }
}
