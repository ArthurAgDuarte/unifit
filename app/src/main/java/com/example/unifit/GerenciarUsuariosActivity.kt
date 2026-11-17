package com.example.unifit

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Tela "Gerenciar Usuários" do administrador (RF22, pág. 43)
class GerenciarUsuariosActivity : TelaBase() {
    lateinit var btnVoltar: View
    lateinit var tilBusca: TextInputLayout
    lateinit var etBusca: TextInputEditText
    lateinit var layoutFiltros: LinearLayout
    lateinit var layoutUsuarios: LinearLayout
    lateinit var txtSemUsuarios: TextView
    lateinit var fabNovoUsuario: View

    var filtroSelecionado = 0 // 0 = Todos, 1 = Ativos, 2 = Inativos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gerenciar_usuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        tilBusca = findViewById(R.id.tilBusca)
        etBusca = findViewById(R.id.etBusca)
        layoutFiltros = findViewById(R.id.layoutFiltros)
        layoutUsuarios = findViewById(R.id.layoutUsuarios)
        txtSemUsuarios = findViewById(R.id.txtSemUsuarios)
        fabNovoUsuario = findViewById(R.id.fabNovoUsuario)

        btnVoltar.setOnClickListener { finish() }
        etBusca.doAfterTextChanged { mostrarUsuarios() } // busca enquanto digita (RF22.2)
        tilBusca.setEndIconOnClickListener {
            // Ícone de filtro: passa para o próximo filtro de status
            filtroSelecionado = (filtroSelecionado + 1) % 3
            mostrarFiltros()
            mostrarUsuarios()
        }
        fabNovoUsuario.setOnClickListener { cadastrarUsuario() }

        mostrarFiltros()
        mostrarUsuarios()
    }

    // Chips com o total de cada categoria (RF22)
    fun mostrarFiltros() {
        val ativos = listaUsuarios.count { it.ativo }
        val opcoes = listOf(
            "Todos (${listaUsuarios.size})",
            "Ativos ($ativos)",
            "Inativos (${listaUsuarios.size - ativos})"
        )
        montarChips(this, layoutFiltros, opcoes, filtroSelecionado) { escolhido ->
            filtroSelecionado = escolhido
            mostrarFiltros()
            mostrarUsuarios()
        }
    }

    fun mostrarUsuarios() {
        layoutUsuarios.removeAllViews()
        val busca = etBusca.text.toString().trim()

        val filtrados = listaUsuarios.filter { usuario ->
            val passaStatus = when (filtroSelecionado) {
                1 -> usuario.ativo
                2 -> !usuario.ativo
                else -> true
            }
            val passaBusca = busca.isEmpty() ||
                usuario.nome.contains(busca, ignoreCase = true) ||
                usuario.detalhe.contains(busca, ignoreCase = true)
            passaStatus && passaBusca
        }
        txtSemUsuarios.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE

        for (usuario in filtrados) {
            val item = layoutInflater.inflate(R.layout.item_card_lista, layoutUsuarios, false)

            // Avatar redondo: azul se ativo, cinza se inativo
            val imgItem = item.findViewById<ImageView>(R.id.imgItem)
            imgItem.setImageResource(R.drawable.ic_perfil)
            imgItem.setBackgroundResource(if (usuario.ativo) R.drawable.bg_circulo_azul_claro else R.drawable.bg_circulo_cinza)
            imgItem.setColorFilter(getColor(if (usuario.ativo) R.color.azul_unifit else R.color.texto_secundario))

            item.findViewById<TextView>(R.id.txtTituloItem).text = usuario.nome
            item.findViewById<TextView>(R.id.txtSubtituloItem).text = usuario.detalhe

            // Etiqueta de status no lugar da seta (RF22.1)
            val txtStatus = item.findViewById<TextView>(R.id.txtStatusItem)
            txtStatus.visibility = View.VISIBLE
            txtStatus.text = if (usuario.ativo) "Ativo" else "Inativo"
            txtStatus.setBackgroundResource(if (usuario.ativo) R.drawable.bg_status_ativo else R.drawable.bg_status_inativo)
            item.findViewById<View>(R.id.imgSetaItem).visibility = View.GONE

            item.setOnClickListener { mostrarOpcoes(usuario) }
            layoutUsuarios.addView(item)
        }
    }

    fun mostrarOpcoes(usuario: Usuario) {
        MaterialAlertDialogBuilder(this)
            .setTitle(usuario.nome)
            .setMessage("${usuario.detalhe}\nStatus: ${if (usuario.ativo) "Ativo" else "Inativo"}")
            .setNegativeButton("Fechar", null)
            .setPositiveButton(if (usuario.ativo) "Desativar" else "Ativar") { _, _ ->
                usuario.ativo = !usuario.ativo
                mostrarFiltros()
                mostrarUsuarios()
            }
            .show()
    }

    // Botão flutuante "+" (RF22.4)
    fun cadastrarUsuario() {
        mostrarDialogoNovoItem(
            this, "Novo usuário", "Nome completo", "Matrícula (ou modalidade, se professor)", "Papel",
            arrayOf("Aluno", "Professor")
        ) { nome, matriculaOuModalidade, papel ->
            val professor = papel == "Professor"
            val detalhe = if (professor) "Professor • $matriculaOuModalidade" else "Mat. $matriculaOuModalidade • Aluno"
            listaUsuarios.add(0, Usuario(nome, detalhe, professor, true))
            Toast.makeText(this, "Usuário cadastrado", Toast.LENGTH_SHORT).show()
            mostrarFiltros()
            mostrarUsuarios()
        }
    }
}
