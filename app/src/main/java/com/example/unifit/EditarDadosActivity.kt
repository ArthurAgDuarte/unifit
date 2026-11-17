package com.example.unifit

import android.os.Bundle
import android.util.Patterns
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Tela "Editar dados" (RF19.1): nome, e-mail, telefone e foto; matrícula bloqueada
class EditarDadosActivity : TelaBase() {
    lateinit var btnVoltar: ImageView
    lateinit var txtIniciais: TextView
    lateinit var btnCamera: ImageView
    lateinit var btnAlterarFoto: TextView
    lateinit var tilNome: TextInputLayout
    lateinit var tilEmail: TextInputLayout
    lateinit var etNome: TextInputEditText
    lateinit var etEmail: TextInputEditText
    lateinit var etTelefone: TextInputEditText
    lateinit var etMatricula: TextInputEditText
    lateinit var btnSalvar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_dados)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        txtIniciais = findViewById(R.id.txtIniciais)
        btnCamera = findViewById(R.id.btnCamera)
        btnAlterarFoto = findViewById(R.id.btnAlterarFoto)
        tilNome = findViewById(R.id.tilNome)
        tilEmail = findViewById(R.id.tilEmail)
        etNome = findViewById(R.id.etNome)
        etEmail = findViewById(R.id.etEmail)
        etTelefone = findViewById(R.id.etTelefone)
        etMatricula = findViewById(R.id.etMatricula)
        btnSalvar = findViewById(R.id.btnSalvar)

        // Preenche com os dados atuais
        etNome.setText(nomeUsuario)
        etEmail.setText(emailUsuario)
        etTelefone.setText(telefoneUsuario)
        etMatricula.setText(matriculaUsuario)
        txtIniciais.text = iniciaisDe(nomeUsuario)

        btnVoltar.setOnClickListener { finish() }
        btnCamera.setOnClickListener { alterarFoto() }
        btnAlterarFoto.setOnClickListener { alterarFoto() }
        btnSalvar.setOnClickListener { salvar() }
    }

    fun alterarFoto() {
        // TODO: abrir a galeria/câmera
        Toast.makeText(this, "Alterar foto: em breve", Toast.LENGTH_SHORT).show()
    }

    fun salvar() {
        val nome = etNome.text.toString().trim()
        val email = etEmail.text.toString().trim()

        tilNome.error = if (nome.isEmpty()) "Digite seu nome" else null
        tilEmail.error = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "E-mail inválido" else null
        if (tilNome.error != null || tilEmail.error != null) return

        nomeUsuario = nome
        emailUsuario = email
        telefoneUsuario = etTelefone.text.toString().trim()
        Toast.makeText(this, "Dados salvos", Toast.LENGTH_SHORT).show()
        finish() // volta para o Perfil, que se atualiza no onResume
    }
}
