package com.example.unifit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import android.content.Intent
import android.widget.TextView
import android.widget.Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        val senha = findViewById<EditText>(R.id.senhaSenha)

        var senhaVisivel = false

        senha.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_UP &&
                event.x > senha.width - senha.paddingRight - 60
            ) {

                if (senhaVisivel) {
                    senha.transformationMethod =
                        PasswordTransformationMethod.getInstance()

                    senhaVisivel = false

                } else {
                    senha.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()

                    senhaVisivel = true
                }

                senha.setSelection(senha.text.length)

                true

            } else {
                false
            }
        }

        // Esqueci minha senha -> Recuperar Senha (RF03.2)
        findViewById<TextView>(R.id.esqueciSenha).setOnClickListener {
            abrirTela(this, RecuperarSenhaActivity::class.java)
        }

        // Criar conta -> Criar Conta (RF03.3)
        findViewById<TextView>(R.id.criarConta).setOnClickListener {
            abrirTela(this, SignUpActivity::class.java)
        }

        // Entrar -> Início. E-mail/matrícula começando com "admin" abre o Painel Admin
        findViewById<Button>(R.id.entrar).setOnClickListener {
            val usuario = findViewById<EditText>(R.id.emailSenha).text.toString().trim()
            if (usuario.startsWith("admin", ignoreCase = true)) {
                abrirTelaLimpandoHistorico(this, DashboardAdmActivity::class.java)
            } else {
                abrirTelaLimpandoHistorico(this, HomeActivity::class.java)
            }
        }
    }
}