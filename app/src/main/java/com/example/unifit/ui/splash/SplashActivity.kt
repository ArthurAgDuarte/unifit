package com.example.unifit.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.example.unifit.ui.base.BaseActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unifit.R
import com.example.unifit.data.FirebaseHelper
import com.example.unifit.data.PreferenciasRepository
import com.example.unifit.data.SeedDebug
import com.example.unifit.databinding.ActivitySplashBinding
import com.example.unifit.ui.main.MainActivity
import com.example.unifit.ui.onboarding.OnboardingActivity
import com.example.unifit.util.Constantes
import com.example.unifit.util.PreferenciasLocais

// Tela de carregamento: login anônimo -> (debug) seed -> preferências -> próxima tela
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val preferenciasRepository = PreferenciasRepository()

    private val handler = Handler(Looper.getMainLooper())
    private val abrirProximaTela = Runnable { navegar() }
    private var inicioCarregamento = 0L

    // Fundo azul (ou preto no alto contraste): ícones claros na barra de status
    override val fundoEscuro: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        binding.btnTentarNovamente.setOnClickListener { carregar() }
        carregar()
    }

    override fun onDestroy() {
        // Se a tela fechar antes da hora, cancela a navegação agendada
        handler.removeCallbacks(abrirProximaTela)
        super.onDestroy()
    }

    private fun carregar() {
        mostrarCarregando()
        inicioCarregamento = SystemClock.elapsedRealtime()
        FirebaseHelper.entrarAnonimo(
            onSuccess = { popularBancoSeDebug() },
            onError = { mostrarErro() }
        )
    }

    // Só em debug: popula o Firestore se ainda estiver vazio.
    // Uma falha aqui não impede o app de abrir (a etapa seguinte também testa a conexão).
    private fun popularBancoSeDebug() {
        SeedDebug.popularSeVazio(
            onSuccess = { carregarPreferencias() },
            onError = { erro ->
                Log.w(Constantes.TAG_FIREBASE, "Seed não executado", erro)
                carregarPreferencias()
            }
        )
    }

    // Traz as preferências do Firestore e guarda a cópia local (lida pelas telas ao abrir)
    private fun carregarPreferencias() {
        preferenciasRepository.carregar(
            onSuccess = { preferencias ->
                PreferenciasLocais.salvarAcessibilidade(this, preferencias)
                agendarNavegacao()
            },
            onError = { mostrarErro() }
        )
    }

    // Garante que o logo fique visível por pelo menos TEMPO_MINIMO_MS (não é um atraso fixo:
    // se o carregamento já demorou mais que isso, navega na hora)
    private fun agendarNavegacao() {
        if (isFinishing || isDestroyed) return
        val decorrido = SystemClock.elapsedRealtime() - inicioCarregamento
        handler.postDelayed(abrirProximaTela, (TEMPO_MINIMO_MS - decorrido).coerceAtLeast(0))
    }

    private fun navegar() {
        // Onboarding só na primeira execução
        val destino = if (PreferenciasLocais.onboardingVisto(this)) {
            MainActivity::class.java
        } else {
            OnboardingActivity::class.java
        }
        startActivity(Intent(this, destino))
        finish() // a Splash não volta com o botão voltar
    }

    private fun mostrarCarregando() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.btnTentarNovamente.visibility = View.GONE
        binding.txtCarregando.setText(R.string.splash_carregando)
    }

    private fun mostrarErro() {
        if (isFinishing || isDestroyed) return
        binding.progressLoading.visibility = View.GONE
        binding.btnTentarNovamente.visibility = View.VISIBLE
        binding.txtCarregando.setText(R.string.splash_erro_conexao)
    }

    companion object {
        private const val TEMPO_MINIMO_MS = 1200L
    }
}
