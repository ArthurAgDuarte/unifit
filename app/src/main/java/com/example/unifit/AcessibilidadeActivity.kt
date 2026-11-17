package com.example.unifit

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch

class AcessibilidadeActivity : TelaBase() {
    lateinit var btnVoltar: ImageView
    lateinit var opcaoVlibras: LinearLayout
    lateinit var opcaoFonteMaior: LinearLayout
    lateinit var opcaoAltoContraste: LinearLayout
    lateinit var switchVlibras: MaterialSwitch
    lateinit var switchFonteMaior: MaterialSwitch
    lateinit var switchAltoContraste: MaterialSwitch
    lateinit var cardPrevia: LinearLayout
    lateinit var txtTituloPrevia: TextView
    lateinit var txtTextoPrevia: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acessibilidade)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        opcaoVlibras = findViewById(R.id.opcaoVlibras)
        opcaoFonteMaior = findViewById(R.id.opcaoFonteMaior)
        opcaoAltoContraste = findViewById(R.id.opcaoAltoContraste)
        switchVlibras = findViewById(R.id.switchVlibras)
        switchFonteMaior = findViewById(R.id.switchFonteMaior)
        switchAltoContraste = findViewById(R.id.switchAltoContraste)
        cardPrevia = findViewById(R.id.cardPrevia)
        txtTituloPrevia = findViewById(R.id.txtTituloPrevia)
        txtTextoPrevia = findViewById(R.id.txtTextoPrevia)

        btnVoltar.setOnClickListener { finish() }

        // Liga cada switch ao que está salvo (antes de colocar os listeners)
        switchVlibras.isChecked = lerPreferencia(this, VLIBRAS)
        switchFonteMaior.isChecked = lerPreferencia(this, FONTE_MAIOR)
        switchAltoContraste.isChecked = lerPreferencia(this, ALTO_CONTRASTE)

        switchVlibras.setOnCheckedChangeListener { _, ligado ->
            salvarPreferencia(this, VLIBRAS, ligado)
            // TODO: integrar o SDK do VLibras para traduzir os conteúdos
            val mensagem = if (ligado) "VLibras ativado" else "VLibras desativado"
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        }
        switchFonteMaior.setOnCheckedChangeListener { _, ligado ->
            salvarPreferencia(this, FONTE_MAIOR, ligado)
            recreate() // recarrega a tela para os textos mudarem de tamanho na hora
        }
        switchAltoContraste.setOnCheckedChangeListener { _, ligado ->
            salvarPreferencia(this, ALTO_CONTRASTE, ligado)
            recreate() // recarrega a tela para as cores mudarem na hora
        }

        // Tocar na linha toda também liga/desliga
        opcaoVlibras.setOnClickListener { switchVlibras.toggle() }
        opcaoFonteMaior.setOnClickListener { switchFonteMaior.toggle() }
        opcaoAltoContraste.setOnClickListener { switchAltoContraste.toggle() }

        mostrarPrevia()
    }

    fun mostrarPrevia() {
        if (switchAltoContraste.isChecked) {
            cardPrevia.setBackgroundResource(R.drawable.bg_card_escuro)
            txtTituloPrevia.setTextColor(getColor(R.color.white))
            txtTextoPrevia.setTextColor(getColor(R.color.white))
            txtTextoPrevia.text = "Com alto contraste ativo, textos e botões ficam mais fáceis de ler."
        } else {
            cardPrevia.setBackgroundResource(R.drawable.bg_card_branco)
            txtTituloPrevia.setTextColor(getColor(R.color.texto_principal))
            txtTextoPrevia.setTextColor(getColor(R.color.texto_secundario))
            txtTextoPrevia.text = "Ative o alto contraste para deixar textos e botões mais fáceis de ler."
        }
    }
}
