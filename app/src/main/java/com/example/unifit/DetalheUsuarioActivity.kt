package com.example.unifit

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unifit.data.FirebaseHelper
import com.example.unifit.util.Constantes
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

// Tela "Detalhe do Usuário" do administrador (RF22.5, RF22.7)
class DetalheUsuarioActivity : TelaBase() {
    lateinit var btnVoltar: View
    lateinit var txtIniciais: TextView
    lateinit var txtNomeUsuario: TextView
    lateinit var txtMatriculaUsuario: TextView
    lateinit var txtPapelTag: TextView
    lateinit var txtCursoTag: TextView
    lateinit var txtStatusDescricao: TextView
    lateinit var txtStatusRotulo: TextView
    lateinit var switchStatus: MaterialSwitch
    lateinit var txtEmailUsuario: TextView
    lateinit var txtTelefoneUsuario: TextView
    lateinit var btnVerTodasReservas: TextView
    lateinit var cardFichaAtual: LinearLayout
    lateinit var btnEditar: MaterialButton
    lateinit var btnDesativar: MaterialButton

    var indiceUsuario = -1
    var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        txtIniciais = findViewById(R.id.txtIniciais)
        txtNomeUsuario = findViewById(R.id.txtNomeUsuario)
        txtMatriculaUsuario = findViewById(R.id.txtMatriculaUsuario)
        txtPapelTag = findViewById(R.id.txtPapelTag)
        txtCursoTag = findViewById(R.id.txtCursoTag)
        txtStatusDescricao = findViewById(R.id.txtStatusDescricao)
        txtStatusRotulo = findViewById(R.id.txtStatusRotulo)
        switchStatus = findViewById(R.id.switchStatus)
        txtEmailUsuario = findViewById(R.id.txtEmailUsuario)
        txtTelefoneUsuario = findViewById(R.id.txtTelefoneUsuario)
        btnVerTodasReservas = findViewById(R.id.btnVerTodasReservas)
        cardFichaAtual = findViewById(R.id.cardFichaAtual)
        btnEditar = findViewById(R.id.btnEditar)
        btnDesativar = findViewById(R.id.btnDesativar)

        indiceUsuario = intent.getIntExtra("indiceUsuario", -1)
        if (indiceUsuario >= 0 && indiceUsuario < listaUsuarios.size) {
            usuario = listaUsuarios[indiceUsuario]
        }

        carregarDados()

        btnVoltar.setOnClickListener { finish() }
        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            usuario?.let {
                if (it.ativo != isChecked) {
                    it.ativo = isChecked
                    atualizarStatusUI(isChecked)
                    salvarStatusNoFirestore(it.nome, isChecked)
                    val msg = if (isChecked) "Acesso do usuário reativado" else "Acesso do usuário suspenso"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnDesativar.setOnClickListener {
            usuario?.let {
                it.ativo = !it.ativo
                atualizarStatusUI(it.ativo)
                salvarStatusNoFirestore(it.nome, it.ativo)
                val msg = if (it.ativo) "Acesso do usuário reativado" else "Acesso do usuário suspenso"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        btnEditar.setOnClickListener {
            // RF22.5: Editar dados do usuário
            abrirTela(this, EditarDadosActivity::class.java)
        }

        btnVerTodasReservas.setOnClickListener {
            // RF22.7: Histórico de reservas do aluno
            abrirTela(this, ReservasActivity::class.java)
        }

        cardFichaAtual.setOnClickListener {
            // RF22.7: Histórico de treinos e ficha do aluno
            abrirTela(this, ProgressoActivity::class.java)
        }
    }

    fun carregarDados() {
        val u = usuario ?: run {
            val nomeExtra = intent.getStringExtra("nome") ?: "Ana Beatriz Souza"
            val detalheExtra = intent.getStringExtra("detalhe") ?: "Mat. 2023104587 • Aluna"
            val profExtra = intent.getBooleanExtra("professor", false)
            val ativoExtra = intent.getBooleanExtra("ativo", true)
            Usuario(nomeExtra, detalheExtra, profExtra, ativoExtra)
        }

        txtNomeUsuario.text = u.nome
        txtIniciais.text = iniciaisDe(u.nome)

        val partes = u.detalhe.split("•").map { it.trim() }
        if (partes.isNotEmpty()) {
            txtMatriculaUsuario.text = partes[0]
        }
        if (partes.size > 1) {
            txtPapelTag.text = if (u.professor) "Professor" else partes[1]
        } else {
            txtPapelTag.text = if (u.professor) "Professor" else "Aluno"
        }

        // Mock e-mail e telefone baseados no usuário
        val primeiroNome = u.nome.split(" ").firstOrNull()?.lowercase() ?: "usuario"
        txtEmailUsuario.text = "$primeiroNome@universidade.edu.br"
        txtTelefoneUsuario.text = "(85) 98765-4321"

        atualizarStatusUI(u.ativo)
    }

    fun atualizarStatusUI(ativo: Boolean) {
        switchStatus.isChecked = ativo
        if (ativo) {
            txtStatusRotulo.text = "Ativo"
            txtStatusRotulo.setTextColor(getColor(R.color.verde_status))
            txtStatusDescricao.text = "Acesso liberado à academia"
            btnDesativar.text = "Desativar usuário"
            btnDesativar.setTextColor(getColor(R.color.vermelho_status))
            btnDesativar.strokeColor = ColorStateList.valueOf(getColor(R.color.vermelho_status))
        } else {
            txtStatusRotulo.text = "Inativo"
            txtStatusRotulo.setTextColor(getColor(R.color.texto_secundario))
            txtStatusDescricao.text = "Acesso suspenso"
            btnDesativar.text = "Ativar usuário"
            btnDesativar.setTextColor(getColor(R.color.azul_unifit))
            btnDesativar.strokeColor = ColorStateList.valueOf(getColor(R.color.azul_unifit))
        }
    }

    private fun salvarStatusNoFirestore(nomeUsuario: String, ativo: Boolean) {
        FirebaseHelper.db.collection(Constantes.COLECAO_USERS)
            .whereEqualTo("nome", nomeUsuario)
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    doc.reference.update("ativo", ativo)
                }
            }
            .addOnFailureListener { erro ->
                Log.e(Constantes.TAG_FIREBASE, "Erro ao atualizar status do usuário no Firestore", erro)
            }
    }
}