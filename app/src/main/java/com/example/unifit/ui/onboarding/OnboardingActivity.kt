package com.example.unifit.ui.onboarding

import android.content.Intent
import android.os.Bundle
import com.example.unifit.ui.base.BaseActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.unifit.R
import com.example.unifit.databinding.ActivityOnboardingBinding
import com.example.unifit.ui.main.MainActivity
import com.example.unifit.util.PreferenciasLocais

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    // Dados dos slides
    private val slides = listOf(
        SlideOnboarding(R.drawable.ic_haltere, R.string.onboarding_titulo_treinos, R.string.onboarding_descricao_treinos),
        SlideOnboarding(R.drawable.ic_calendario_contorno, R.string.onboarding_titulo_aulas, R.string.onboarding_descricao_aulas),
        SlideOnboarding(R.drawable.ic_mapa, R.string.onboarding_titulo_academia, R.string.onboarding_descricao_academia)
    )

    // Atualiza botão e bolinhas quando o slide muda (por toque ou por arrastar)
    private val aoMudarPagina = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            atualizarTela(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        binding.viewPagerOnboarding.adapter = OnboardingAdapter(slides)
        binding.viewPagerOnboarding.registerOnPageChangeCallback(aoMudarPagina)

        binding.btnComecar.setOnClickListener {
            val atual = binding.viewPagerOnboarding.currentItem
            if (atual == slides.lastIndex) {
                finalizarOnboarding()
            } else {
                binding.viewPagerOnboarding.currentItem = atual + 1
            }
        }
        binding.btnPular.setOnClickListener { finalizarOnboarding() }

        atualizarTela(binding.viewPagerOnboarding.currentItem)
    }

    override fun onDestroy() {
        binding.viewPagerOnboarding.unregisterOnPageChangeCallback(aoMudarPagina)
        super.onDestroy()
    }

    private fun atualizarTela(pagina: Int) {
        val ultima = pagina == slides.lastIndex
        binding.btnComecar.setText(if (ultima) R.string.onboarding_comecar else R.string.onboarding_proximo)
        atualizarIndicadores(pagina)
    }

    // Página atual: pílula de 24dp | demais: bolinha de 8dp
    private fun atualizarIndicadores(pagina: Int) {
        val indicadores = listOf(binding.indicador1, binding.indicador2, binding.indicador3)
        indicadores.forEachIndexed { indice, indicador ->
            val ativo = indice == pagina
            indicador.layoutParams.width = dpParaPx(if (ativo) 24 else 8)
            indicador.setBackgroundResource(
                if (ativo) R.drawable.bg_indicador_ativo else R.drawable.bg_indicador_inativo
            )
            indicador.requestLayout()
        }
    }

    // "Começar" e "Pular": grava a flag e abre a tela principal limpando a pilha.
    // finishAffinity() fecha todas as telas do app (mesmo padrão de abrirTelaLimpandoHistorico).
    private fun finalizarOnboarding() {
        PreferenciasLocais.marcarOnboardingVisto(this)
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun dpParaPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
