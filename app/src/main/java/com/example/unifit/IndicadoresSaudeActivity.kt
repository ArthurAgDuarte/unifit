package com.example.unifit

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate

// Tela "Indicadores de Saúde" (RF17, pág. 36)
class IndicadoresSaudeActivity : TelaBase() {
    lateinit var txtDataHoje: TextView
    lateinit var layoutIndicadores: LinearLayout
    lateinit var txtDicaSaude: TextView
    lateinit var btnNotificacoes: View
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_indicadores_saude)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        txtDataHoje = findViewById(R.id.txtDataHoje)
        layoutIndicadores = findViewById(R.id.layoutIndicadores)
        txtDicaSaude = findViewById(R.id.txtDicaSaude)
        btnNotificacoes = findViewById(R.id.btnNotificacoes)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Esta tela fica "dentro" da aba Perfil (como no protótipo)
        configurarMenuInferior(this, bottomNavigation, R.id.nav_perfil, false)
        btnNotificacoes.setOnClickListener { abrirTela(this, NotificacoesActivity::class.java) }

        txtDataHoje.text = "Hoje, ${dataPorExtenso(LocalDate.now())}"
        mostrarIndicadores()
    }

    fun mostrarIndicadores() {
        layoutIndicadores.removeAllViews()

        for (indicador in listaIndicadores) {
            val card = layoutInflater.inflate(R.layout.item_indicador, layoutIndicadores, false)
            val cor = getColor(indicador.cor)
            val percentual = indicador.percentual()

            val imgIndicador = card.findViewById<ImageView>(R.id.imgIndicador)
            imgIndicador.setImageResource(indicador.icone)
            imgIndicador.setColorFilter(cor)
            imgIndicador.background.mutate().setTint(getColor(indicador.corFundo))

            card.findViewById<TextView>(R.id.txtNomeIndicador).text = indicador.nome
            card.findViewById<TextView>(R.id.txtValorIndicador).text = formatarNumero(indicador.valor)
            card.findViewById<TextView>(R.id.txtUnidadeIndicador).text = indicador.unidade
            card.findViewById<TextView>(R.id.txtMetaIndicador).text = "Meta: ${formatarNumero(indicador.meta)} ${indicador.unidade}"

            val txtPercentual = card.findViewById<TextView>(R.id.txtPercentualIndicador)
            txtPercentual.text = "$percentual%"
            txtPercentual.setTextColor(cor)

            val progresso = card.findViewById<LinearProgressIndicator>(R.id.progressoIndicador)
            progresso.progress = percentual
            progresso.setIndicatorColor(cor)

            card.findViewById<View>(R.id.btnAdicionarIndicador).setOnClickListener { registrarValor(indicador) }
            layoutIndicadores.addView(card)
        }

        // Dica do indicador mais longe da meta (RF17.3)
        val maisAtrasado = listaIndicadores.minBy { it.percentual() }
        txtDicaSaude.text = if (maisAtrasado.percentual() >= 100) "Parabéns! Você bateu todas as metas de hoje." else maisAtrasado.dica
    }

    // Botão "+": registrar novo valor (RF17.2)
    fun registrarValor(indicador: Indicador) {
        val view = layoutInflater.inflate(R.layout.dialog_valor, null)
        val tilValor = view.findViewById<TextInputLayout>(R.id.tilValor)
        val etValor = view.findViewById<TextInputEditText>(R.id.etValor)
        tilValor.hint = if (indicador.somaAoAdicionar) "Quanto adicionar (${indicador.unidade})" else "Novo valor (${indicador.unidade})"

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(indicador.nome)
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Registrar", null)
            .show()

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val valor = etValor.text.toString().replace(',', '.').toDoubleOrNull()
            if (valor == null || valor <= 0) {
                tilValor.error = "Digite um valor válido"
                return@setOnClickListener
            }
            indicador.valor = if (indicador.somaAoAdicionar) indicador.valor + valor else valor
            mostrarIndicadores()
            dialogo.dismiss()
        }
    }
}
