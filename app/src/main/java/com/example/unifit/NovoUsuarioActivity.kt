package com.example.unifit

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.unifit.data.FirebaseHelper
import com.example.unifit.util.Constantes
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldValue

// Tela "Novo Usuário" (RF22.4, RF22.6)
class NovoUsuarioActivity : TelaBase() {

    lateinit var btnVoltar: View
    lateinit var tilNome: TextInputLayout
    lateinit var etNome: TextInputEditText
    lateinit var tilEmail: TextInputLayout
    lateinit var etEmail: TextInputEditText
    lateinit var tilMatricula: TextInputLayout
    lateinit var etMatricula: TextInputEditText
    lateinit var btnPapelAluno: TextView
    lateinit var btnPapelProfessor: TextView
    lateinit var layoutSecaoModalidades: LinearLayout
    lateinit var chipSpinning: Chip
    lateinit var chipYoga: Chip
    lateinit var chipFuncional: Chip
    lateinit var chipPilates: Chip
    lateinit var chipZumba: Chip
    lateinit var btnCadastrar: MaterialButton

    var eProfessor = true // Padrão selecionado como no protótipo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_novo_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        tilNome = findViewById(R.id.tilNome)
        etNome = findViewById(R.id.etNome)
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilMatricula = findViewById(R.id.tilMatricula)
        etMatricula = findViewById(R.id.etMatricula)
        btnPapelAluno = findViewById(R.id.btnPapelAluno)
        btnPapelProfessor = findViewById(R.id.btnPapelProfessor)
        layoutSecaoModalidades = findViewById(R.id.layoutSecaoModalidades)
        chipSpinning = findViewById(R.id.chipSpinning)
        chipYoga = findViewById(R.id.chipYoga)
        chipFuncional = findViewById(R.id.chipFuncional)
        chipPilates = findViewById(R.id.chipPilates)
        chipZumba = findViewById(R.id.chipZumba)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        // Limpeza de erros ao digitar
        etNome.doAfterTextChanged { tilNome.error = null }
        etEmail.doAfterTextChanged { tilEmail.error = null }
        etMatricula.doAfterTextChanged { tilMatricula.error = null }

        btnVoltar.setOnClickListener { finish() }

        // Seleção de Papel (Aluno x Professor)
        btnPapelAluno.setOnClickListener { selecionarPapel(isProfessor = false) }
        btnPapelProfessor.setOnClickListener { selecionarPapel(isProfessor = true) }

        btnCadastrar.setOnClickListener { salvarUsuario() }

        // Inicia com Professor selecionado como na imagem
        selecionarPapel(isProfessor = true)
    }

    fun selecionarPapel(isProfessor: Boolean) {
        eProfessor = isProfessor
        if (isProfessor) {
            btnPapelProfessor.setBackgroundResource(R.drawable.bg_papel_selecionado)
            btnPapelProfessor.setTextColor(getColor(R.color.white))
            btnPapelAluno.setBackgroundResource(R.drawable.bg_papel_deselecionado)
            btnPapelAluno.setTextColor(getColor(R.color.texto_principal))
            layoutSecaoModalidades.visibility = View.VISIBLE
        } else {
            btnPapelAluno.setBackgroundResource(R.drawable.bg_papel_selecionado)
            btnPapelAluno.setTextColor(getColor(R.color.white))
            btnPapelProfessor.setBackgroundResource(R.drawable.bg_papel_deselecionado)
            btnPapelProfessor.setTextColor(getColor(R.color.texto_principal))
            layoutSecaoModalidades.visibility = View.GONE
        }
    }

    fun salvarUsuario() {
        val nome = etNome.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val matricula = etMatricula.text.toString().trim()

        tilNome.error = if (nome.isEmpty()) "Informe o nome completo" else null
        tilEmail.error = when {
            email.isEmpty() -> "Informe o e-mail institucional"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "E-mail inválido"
            else -> null
        }
        tilMatricula.error = if (matricula.isEmpty()) "Informe a matrícula ou função" else null

        if (tilNome.error != null || tilEmail.error != null || tilMatricula.error != null) {
            return
        }

        val modalidadesSelecionadas = if (eProfessor) {
            listOf(chipSpinning, chipYoga, chipFuncional, chipPilates, chipZumba)
                .filter { it.isChecked }
                .map { it.text.toString() }
        } else emptyList()

        val detalhe = if (eProfessor) {
            if (modalidadesSelecionadas.isNotEmpty()) {
                "Professor • ${modalidadesSelecionadas.joinToString(", ")}"
            } else {
                "Professor • $matricula"
            }
        } else {
            "Mat. $matricula • Aluno"
        }

        // 1) Grava no estado em memória para atualização instantânea nas telas
        listaUsuarios.add(0, Usuario(nome, detalhe, eProfessor, true))

        // 2) Persiste no Cloud Firestore (Coleção "users")
        val dadosFirestore = hashMapOf(
            "nome" to nome,
            "email" to email,
            "matricula" to matricula,
            "papel" to if (eProfessor) "Professor" else "Aluno",
            "detalhe" to detalhe,
            "modalidades" to modalidadesSelecionadas,
            "ativo" to true,
            "criadoEm" to FieldValue.serverTimestamp()
        )

        FirebaseHelper.db.collection(Constantes.COLECAO_USERS)
            .add(dadosFirestore)
            .addOnSuccessListener {
                Log.d(Constantes.TAG_FIREBASE, "Usuário registrado no Firestore com sucesso")
            }
            .addOnFailureListener { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao gravar usuário no Firestore", erro)
            }

        Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}