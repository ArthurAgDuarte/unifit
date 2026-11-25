package com.example.unifit.ui.acessibilidade

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unifit.R
import com.example.unifit.data.PreferenciasRepository
import com.example.unifit.databinding.ActivityAcessibilidadeBinding
import com.example.unifit.model.Preferencias
import com.example.unifit.ui.base.BaseActivity
import com.example.unifit.util.Constantes
import com.example.unifit.util.Mensagens
import com.example.unifit.util.PreferenciasLocais

// Configurações de acessibilidade. Cada mudança é salva no aparelho (aplicada ao abrir as telas)
// e no Firestore (mantida entre instalações). A pré-visualização mostra o efeito na hora;
// as outras telas abertas se recriam com a nova opção quando o usuário volta para elas.
class AcessibilidadeActivity : BaseActivity() {

    private lateinit var binding: ActivityAcessibilidadeBinding
    private val preferenciasRepository = PreferenciasRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcessibilidadeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        binding.btnVoltar.setOnClickListener { finish() }

        // Marca os switches com o que está salvo ANTES de ligar os listeners (senão salvaria de novo)
        val salvas = PreferenciasLocais.lerAcessibilidade(this)
        binding.switchVlibras.isChecked = salvas.vlibras
        binding.switchFonteMaior.isChecked = salvas.fonteMaior
        binding.switchAltoContraste.isChecked = salvas.altoContraste

        binding.switchVlibras.setOnCheckedChangeListener { _, _ -> salvar() } // integração do VLibras: depois
        binding.switchFonteMaior.setOnCheckedChangeListener { _, _ -> salvar() }
        binding.switchAltoContraste.setOnCheckedChangeListener { _, _ -> salvar() }

        // Tocar na linha toda também liga/desliga
        binding.opcaoVlibras.setOnClickListener { binding.switchVlibras.toggle() }
        binding.opcaoFonteMaior.setOnClickListener { binding.switchFonteMaior.toggle() }
        binding.opcaoAltoContraste.setOnClickListener { binding.switchAltoContraste.toggle() }

        mostrarPrevia()
    }

    private fun preferenciasDosSwitches() = Preferencias(
        fonteMaior = binding.switchFonteMaior.isChecked,
        altoContraste = binding.switchAltoContraste.isChecked,
        vlibras = binding.switchVlibras.isChecked
    )

    private fun salvar() {
        val preferencias = preferenciasDosSwitches()

        // 1) No aparelho: é o que a BaseActivity lê ao abrir/voltar para cada tela
        PreferenciasLocais.salvarAcessibilidade(this, preferencias)
        mostrarPrevia()

        // 2) No Firestore (sem internet, o Firestore guarda e envia quando a conexão voltar)
        preferenciasRepository.salvar(
            preferencias,
            onSuccess = { },
            onError = { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao salvar preferências", erro)
                if (!isFinishing && !isDestroyed) {
                    Mensagens.mostrar(binding.root, R.string.erro_salvar_preferencias)
                }
            }
        )
    }

    // Card de exemplo com as cores e o tamanho de texto que os switches escolheram.
    // Usa cores fixas (e não as do tema) porque a tela atual ainda está com o tema anterior.
    private fun mostrarPrevia() {
        val contraste = binding.switchAltoContraste.isChecked
        if (contraste) {
            binding.cardPrevia.setBackgroundResource(R.drawable.bg_card_escuro)
            binding.txtTituloPrevia.setTextColor(getColor(R.color.contraste_destaque))
            binding.txtTextoPrevia.setTextColor(getColor(R.color.contraste_texto))
            binding.txtTextoPrevia.setText(R.string.previa_texto_contraste)
        } else {
            binding.cardPrevia.setBackgroundResource(R.drawable.bg_card_branco)
            binding.txtTituloPrevia.setTextColor(getColor(R.color.texto_principal))
            binding.txtTextoPrevia.setTextColor(getColor(R.color.texto_secundario))
            binding.txtTextoPrevia.setText(R.string.previa_texto_normal)
        }

        // Fonte: se esta tela já abriu com a fonte maior, o "sp" já vem ×1,2; a conta desfaz/refaz isso
        val escalaDesejada = if (binding.switchFonteMaior.isChecked) ESCALA_FONTE_MAIOR else 1f
        val escalaAplicada = if (fonteMaiorAplicada) ESCALA_FONTE_MAIOR else 1f
        val fator = escalaDesejada / escalaAplicada
        binding.txtTituloPrevia.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAMANHO_TITULO_SP * fator)
        binding.txtTextoPrevia.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAMANHO_TEXTO_SP * fator)
    }

    companion object {
        // Mesmos tamanhos de activity_acessibilidade.xml
        private const val TAMANHO_TITULO_SP = 20f
        private const val TAMANHO_TEXTO_SP = 14f
    }
}
