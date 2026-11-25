package com.example.unifit.ui.treinos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unifit.R
import com.example.unifit.data.FichaRepository
import com.example.unifit.databinding.FragmentTreinosBinding
import com.example.unifit.model.Ficha
import com.example.unifit.ui.detalhetreino.DetalheTreinoActivity
import com.example.unifit.ui.main.MainActivity
import com.example.unifit.util.Constantes
import com.example.unifit.util.Fotos
import java.time.DayOfWeek
import java.time.LocalDate

// Aba "Meus Treinos": seletor da semana atual + fichas do dia selecionado
class TreinosFragment : Fragment() {

    private var _binding: FragmentTreinosBinding? = null
    private val binding get() = _binding!!

    private val fichaRepository = FichaRepository()
    private lateinit var diaAdapter: DiaAdapter
    private lateinit var fichaAdapter: FichaAdapter

    private lateinit var hoje: LocalDate
    private lateinit var semana: List<LocalDate>
    private lateinit var diaSelecionado: LocalDate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTreinosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Semana de segunda a domingo calculada a partir de hoje (nada de datas fixas)
        hoje = LocalDate.now()
        val segunda = hoje.with(DayOfWeek.MONDAY)
        semana = List(7) { segunda.plusDays(it.toLong()) }

        // Ao girar a tela, mantém o dia que estava selecionado (se ainda for desta semana)
        val diaSalvo = savedInstanceState?.getString(CHAVE_DIA)?.let { LocalDate.parse(it) }
        diaSelecionado = if (diaSalvo != null && diaSalvo in semana) diaSalvo else hoje

        diaAdapter = DiaAdapter { dia -> selecionarDia(dia) }
        binding.rvDias.layoutManager = GridLayoutManager(requireContext(), 7) // uma linha com 7 colunas
        binding.rvDias.adapter = diaAdapter

        fichaAdapter = FichaAdapter { ficha -> abrirDetalhe(ficha) }
        binding.rvFichas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFichas.adapter = fichaAdapter

        // Fora do escopo por enquanto
        binding.btnNovaFicha.setOnClickListener { mostrarMensagem(R.string.em_breve) }
        binding.cardDica.setOnClickListener { mostrarMensagem(R.string.em_breve) }
        binding.atalhoProgresso.setOnClickListener { mostrarMensagem(R.string.em_breve) }
        binding.atalhoHistorico.setOnClickListener { mostrarMensagem(R.string.em_breve) }

        selecionarDia(diaSelecionado)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::diaSelecionado.isInitialized) outState.putString(CHAVE_DIA, diaSelecionado.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selecionarDia(dia: LocalDate) {
        diaSelecionado = dia
        diaAdapter.submitList(semana.map { DiaSemana(it, selecionado = it == dia, hoje = it == hoje) })

        // "Fichas Ativas • Qua"
        val nomeCurto = resources.getStringArray(R.array.dias_semana_curto)[dia.dayOfWeek.value - 1]
        binding.txtFichasAtivas.text = getString(R.string.fichas_ativas_dia, nomeCurto)

        carregarFichas(dia)
    }

    private fun carregarFichas(dia: LocalDate) {
        mostrarCarregando()
        fichaRepository.buscarFichasDoDia(
            dia.dayOfWeek.value, // 1 = segunda ... 7 = domingo, igual ao campo diasSemana
            onSuccess = { fichas ->
                // Ignora a resposta se a tela foi fechada ou se o usuário já tocou em outro dia
                if (_binding == null || dia != diaSelecionado) return@buscarFichasDoDia
                val ehHoje = dia == hoje
                val itens = fichas.mapIndexed { posicao, ficha -> FichaItem(ficha, ehHoje, Fotos.daFicha(posicao)) }
                mostrarFichas(itens)
            },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao buscar fichas do dia $dia", erro)
                if (_binding == null || dia != diaSelecionado) return@buscarFichasDoDia
                binding.progressFichas.visibility = View.GONE
                mostrarMensagem(R.string.erro_carregar_fichas)
            }
        )
    }

    private fun mostrarCarregando() {
        binding.progressFichas.visibility = View.VISIBLE
        binding.rvFichas.visibility = View.GONE
        binding.txtSemFichas.visibility = View.GONE
    }

    private fun mostrarFichas(itens: List<FichaItem>) {
        binding.progressFichas.visibility = View.GONE
        fichaAdapter.submitList(itens)
        binding.rvFichas.visibility = if (itens.isEmpty()) View.GONE else View.VISIBLE
        binding.txtSemFichas.visibility = if (itens.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun abrirDetalhe(ficha: Ficha) {
        // O detalhe busca a ficha no Firestore pelo id
        val intent = Intent(requireContext(), DetalheTreinoActivity::class.java)
        intent.putExtra(Constantes.EXTRA_FICHA_ID, ficha.id)
        startActivity(intent)
    }

    private fun mostrarMensagem(@StringRes mensagem: Int) {
        (activity as? MainActivity)?.mostrarMensagem(mensagem)
    }

    companion object {
        private const val CHAVE_DIA = "dia_selecionado"
    }
}
