package com.example.unifit

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class NotificacoesActivity : TelaBase() {
    lateinit var txtNaoLidas: TextView
    lateinit var btnMarcarTodas: TextView
    lateinit var txtSecaoHoje: TextView
    lateinit var txtSecaoOntem: TextView
    lateinit var layoutHoje: LinearLayout
    lateinit var layoutOntem: LinearLayout
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notificacoes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // CONSUMED para o menu inferior não somar o espaço da barra de navegação de novo
            WindowInsetsCompat.CONSUMED
        }

        txtNaoLidas = findViewById(R.id.txtNaoLidas)
        btnMarcarTodas = findViewById(R.id.btnMarcarTodas)
        txtSecaoHoje = findViewById(R.id.txtSecaoHoje)
        txtSecaoOntem = findViewById(R.id.txtSecaoOntem)
        layoutHoje = findViewById(R.id.layoutHoje)
        layoutOntem = findViewById(R.id.layoutOntem)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Esta tela fica "dentro" do Início: o menu mostra Início marcado
        configurarMenuInferior(this, bottomNavigation, R.id.nav_inicio, false)

        btnMarcarTodas.setOnClickListener {
            for (notificacao in listaNotificacoes) {
                notificacao.lida = true
            }
            mostrarNotificacoes()
        }

        mostrarNotificacoes()
    }

    fun mostrarNotificacoes() {
        val naoLidas = listaNotificacoes.count { !it.lida }
        txtNaoLidas.text = when (naoLidas) {
            0 -> "Nenhuma não lida"
            1 -> "1 não lida"
            else -> "$naoLidas não lidas"
        }
        btnMarcarTodas.visibility = if (naoLidas > 0) View.VISIBLE else View.GONE

        mostrarSecao("HOJE", layoutHoje, txtSecaoHoje)
        mostrarSecao("ONTEM", layoutOntem, txtSecaoOntem)
    }

    fun mostrarSecao(dia: String, layout: LinearLayout, txtSecao: TextView) {
        layout.removeAllViews()
        val notificacoesDoDia = listaNotificacoes.filter { it.dia == dia }
        txtSecao.visibility = if (notificacoesDoDia.isEmpty()) View.GONE else View.VISIBLE

        for (notificacao in notificacoesDoDia) {
            val item = layoutInflater.inflate(R.layout.item_notificacao, layout, false)
            val imgNotificacao = item.findViewById<ImageView>(R.id.imgNotificacao)

            imgNotificacao.setImageResource(notificacao.icone)
            item.findViewById<TextView>(R.id.txtTituloNotificacao).text = notificacao.titulo
            item.findViewById<TextView>(R.id.txtDetalheNotificacao).text =
                "${notificacao.mensagem} • ${notificacao.quando}"

            if (notificacao.lida) {
                // Lida: card e ícone cinza, sem a bolinha azul
                item.setBackgroundResource(R.drawable.bg_card_lido)
                imgNotificacao.setBackgroundResource(R.drawable.bg_icone_lido)
                imgNotificacao.setColorFilter(getColor(R.color.texto_secundario))
                item.findViewById<View>(R.id.pontoNaoLida).visibility = View.GONE
            }

            // Tocar na notificação marca como lida
            item.setOnClickListener {
                notificacao.lida = true
                mostrarNotificacoes()
            }

            layout.addView(item)
        }
    }
}
