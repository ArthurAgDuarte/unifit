package com.example.unifit

import com.example.unifit.ui.acessibilidade.AcessibilidadeActivity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class PerfilActivity : TelaBase() {
    lateinit var txtIniciais: TextView
    lateinit var txtNomeUsuario: TextView
    lateinit var txtCurso: TextView
    lateinit var txtTotalTreinos: TextView
    lateinit var txtTotalMinutos: TextView
    lateinit var txtTotalFichas: TextView
    lateinit var opcaoEditarPerfil: LinearLayout
    lateinit var opcaoLembretes: LinearLayout
    lateinit var switchLembretes: MaterialSwitch
    lateinit var opcaoAcessibilidade: LinearLayout
    lateinit var opcaoAjuda: LinearLayout
    lateinit var cardNumeros: LinearLayout
    lateinit var opcaoSobre: LinearLayout
    lateinit var opcaoSair: LinearLayout
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        txtIniciais = findViewById(R.id.txtIniciais)
        txtNomeUsuario = findViewById(R.id.txtNomeUsuario)
        txtCurso = findViewById(R.id.txtCurso)
        txtTotalTreinos = findViewById(R.id.txtTotalTreinos)
        txtTotalMinutos = findViewById(R.id.txtTotalMinutos)
        txtTotalFichas = findViewById(R.id.txtTotalFichas)
        opcaoEditarPerfil = findViewById(R.id.opcaoEditarPerfil)
        opcaoLembretes = findViewById(R.id.opcaoLembretes)
        switchLembretes = findViewById(R.id.switchLembretes)
        opcaoAcessibilidade = findViewById(R.id.opcaoAcessibilidade)
        opcaoAjuda = findViewById(R.id.opcaoAjuda)
        cardNumeros = findViewById(R.id.cardNumeros)
        opcaoSobre = findViewById(R.id.opcaoSobre)
        opcaoSair = findViewById(R.id.opcaoSair)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        configurarMenuInferior(this, bottomNavigation, R.id.nav_perfil)

        switchLembretes.isChecked = lembretesAtivos
        switchLembretes.setOnCheckedChangeListener { _, ligado ->
            lembretesAtivos = ligado
            val mensagem = if (ligado) "Lembretes ativados" else "Lembretes desativados"
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        }
        // Tocar na linha toda também liga/desliga
        opcaoLembretes.setOnClickListener { switchLembretes.toggle() }

        opcaoEditarPerfil.setOnClickListener { abrirTela(this, EditarDadosActivity::class.java) } // RF19.1
        opcaoAcessibilidade.setOnClickListener {
            startActivity(Intent(this, AcessibilidadeActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.opcaoIndicadores).setOnClickListener {
            abrirTela(this, IndicadoresSaudeActivity::class.java) // RF17
        }
        opcaoAjuda.setOnClickListener { abrirTela(this, GymBotActivity::class.java) } // RF19.4
        cardNumeros.setOnClickListener { abrirTela(this, ProgressoActivity::class.java) }
        opcaoSobre.setOnClickListener { mostrarSobre() }
        opcaoSair.setOnClickListener { confirmarSair() }

    }

    override fun onResume() {
        super.onResume()
        // Atualiza ao voltar do Editar dados ou de um treino concluído
        mostrarPerfil()
    }

    fun mostrarPerfil() {
        txtNomeUsuario.text = nomeUsuario
        txtCurso.text = cursoUsuario

        txtIniciais.text = iniciaisDe(nomeUsuario)

        txtTotalTreinos.text = historicoTreinos.size.toString()
        txtTotalMinutos.text = historicoTreinos.sumOf { it.minutos }.toString()
        txtTotalFichas.text = listaFichas.size.toString()
    }

    fun mostrarSobre() {
        MaterialAlertDialogBuilder(this)
            .setTitle("UniFit")
            .setMessage("Seu treino, sua universidade.\n\nVersão 1.0")
            .setPositiveButton("Fechar", null)
            .show()
    }

    fun confirmarSair() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sair do UniFit?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Sair") { _, _ ->
                // Encerra a sessão e volta para o Login (RF19.5)
                abrirTelaLimpandoHistorico(this, LoginActivity::class.java)
            }
            .show()
    }
}
