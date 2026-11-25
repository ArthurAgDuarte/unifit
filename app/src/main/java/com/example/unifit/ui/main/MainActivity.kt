package com.example.unifit.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.unifit.R
import com.example.unifit.data.NotificacaoRepository
import com.example.unifit.databinding.ActivityMainBinding
import com.example.unifit.ui.base.BaseActivity
import com.example.unifit.ui.notificacoes.NotificacoesActivity
import com.example.unifit.ui.perfil.PerfilFragment
import com.example.unifit.ui.treinos.TreinosFragment
import com.example.unifit.util.Constantes
import com.example.unifit.util.Mensagens
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.firebase.firestore.ListenerRegistration

// Tela principal: cabeçalho com o sino + menu inferior; cada aba é um Fragment
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var abaAtual = R.id.nav_treinos

    // Badge do sino com o total de não lidas, atualizado em tempo real
    private val notificacaoRepository = NotificacaoRepository()
    private lateinit var badge: BadgeDrawable
    private var registroBadge: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            // CONSUMED para o menu inferior não somar o espaço da barra de navegação de novo
            WindowInsetsCompat.CONSUMED
        }

        binding.btnNotificacoes.setOnClickListener {
            startActivity(Intent(this, NotificacoesActivity::class.java))
        }
        criarBadge()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            mostrarAba(item.itemId)
            true
        }
        // Tocar de novo na aba atual não recria o Fragment
        binding.bottomNavigation.setOnItemReselectedListener { }

        if (savedInstanceState == null) {
            // Primeira abertura: começa em "Meus Treinos" (dispara o listener acima)
            binding.bottomNavigation.selectedItemId = R.id.nav_treinos
        } else {
            // Ao girar a tela o FragmentManager restaura o Fragment; só falta o título
            abaAtual = savedInstanceState.getInt(CHAVE_ABA, R.id.nav_treinos)
            binding.txtTituloTela.setText(tituloDa(abaAtual))
        }
    }

    override fun onStart() {
        super.onStart()
        // Só as não lidas (whereEqualTo("lida", false)); o Firestore avisa a cada mudança
        registroBadge = notificacaoRepository.ouvirTotalNaoLidas(
            onChange = { total -> atualizarBadge(total) },
            onError = { erro -> Log.e(Constantes.TAG_FIREBASE, "Erro no listener do badge", erro) }
        )
    }

    override fun onStop() {
        registroBadge?.remove()
        registroBadge = null
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CHAVE_ABA, abaAtual)
    }

    // BadgeUtils ainda é marcada como experimental na biblioteca Material
    @OptIn(markerClass = [ExperimentalBadgeUtils::class])
    private fun criarBadge() {
        badge = BadgeDrawable.create(this).apply {
            isVisible = false
            // Puxa o badge para dentro do círculo do sino (por padrão ele fica bem na quina)
            horizontalOffset = dpParaPx(6)
            verticalOffset = dpParaPx(6)
        }
        // O badge precisa do tamanho final do sino, então espera o primeiro layout
        binding.btnNotificacoes.post {
            BadgeUtils.attachBadgeDrawable(badge, binding.btnNotificacoes)
        }
    }

    private fun atualizarBadge(total: Int) {
        badge.number = total
        badge.isVisible = total > 0
        // Leitor de tela: "Notificações, 3 não lidas"
        binding.btnNotificacoes.contentDescription = if (total > 0) {
            resources.getQuantityString(R.plurals.notificacoes_descricao_badge, total, total)
        } else {
            getString(R.string.notificacoes)
        }
    }

    private fun mostrarAba(itemId: Int) {
        abaAtual = itemId
        binding.txtTituloTela.setText(tituloDa(itemId))

        val fragment: Fragment = when (itemId) {
            R.id.nav_treinos -> TreinosFragment()
            R.id.nav_perfil -> PerfilFragment()
            else -> EmBreveFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerAbas, fragment)
            .commit()
    }

    @StringRes
    private fun tituloDa(itemId: Int): Int = when (itemId) {
        R.id.nav_inicio -> R.string.aba_inicio
        R.id.nav_aulas -> R.string.aba_aulas
        R.id.nav_mapa -> R.string.aba_mapa
        R.id.nav_perfil -> R.string.aba_perfil
        else -> R.string.aba_treinos
    }

    // Usado pelos Fragments: Snackbar acima do menu inferior
    fun mostrarMensagem(@StringRes mensagem: Int) {
        Mensagens.mostrar(binding.root, mensagem, binding.bottomNavigation)
    }

    private fun dpParaPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    companion object {
        private const val CHAVE_ABA = "aba_atual"
    }
}
