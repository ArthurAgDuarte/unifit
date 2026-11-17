package com.example.unifit

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Tela "Gerenciar Aulas" do administrador (RF23, pág. 44)
class GerenciarAulasActivity : TelaBase() {
    lateinit var btnVoltar: View
    lateinit var layoutPeriodos: LinearLayout
    lateinit var layoutAulasAdmin: LinearLayout
    lateinit var txtSemAulas: TextView
    lateinit var fabNovaAula: View

    val periodos = listOf("Hoje", "Amanhã", "Esta semana")
    var periodoSelecionado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gerenciar_aulas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        layoutPeriodos = findViewById(R.id.layoutPeriodos)
        layoutAulasAdmin = findViewById(R.id.layoutAulasAdmin)
        txtSemAulas = findViewById(R.id.txtSemAulas)
        fabNovaAula = findViewById(R.id.fabNovaAula)

        btnVoltar.setOnClickListener { finish() }
        fabNovaAula.setOnClickListener { mostrarFormularioNovaAula() }
    }

    override fun onResume() {
        super.onResume()
        mostrarPeriodos()
        mostrarAulas()
    }

    fun mostrarPeriodos() {
        montarChips(this, layoutPeriodos, periodos, periodoSelecionado) { escolhido ->
            periodoSelecionado = escolhido
            mostrarPeriodos()
            mostrarAulas()
        }
    }

    fun mostrarAulas() {
        layoutAulasAdmin.removeAllViews()
        val indices = listaAulas.indices.filter {
            when (periodoSelecionado) {
                0 -> listaAulas[it].diaOffset == 0
                1 -> listaAulas[it].diaOffset == 1
                else -> listaAulas[it].diaOffset in 0..6
            }
        }.sortedWith(compareBy({ listaAulas[it].diaOffset }, { listaAulas[it].horario }))
        txtSemAulas.visibility = if (indices.isEmpty()) View.VISIBLE else View.GONE

        for (i in indices) {
            val item = layoutInflater.inflate(R.layout.item_card_lista, layoutAulasAdmin, false)
            preencherCardAula(this, item, listaAulas[i], true) // RF23.1: vagas ocupadas/total
            if (periodoSelecionado == 2) {
                // Na semana, mostra também o dia
                val txtTitulo = item.findViewById<TextView>(R.id.txtTituloItem)
                txtTitulo.text = "${txtTitulo.text} • ${nomeDoDia(listaAulas[i].diaOffset)}"
            }
            item.setOnClickListener {
                val intent = Intent(this, DetalheAulaActivity::class.java)
                intent.putExtra("indiceAula", i)
                startActivity(intent)
            }
            layoutAulasAdmin.addView(item)
        }
    }

    // Formulário "Nova aula" (RF23.2 a RF23.6)
    fun mostrarFormularioNovaAula() {
        val view = layoutInflater.inflate(R.layout.sheet_nova_aula, null)
        val tilNomeAula = view.findViewById<TextInputLayout>(R.id.tilNomeAula)
        val tilProfessor = view.findViewById<TextInputLayout>(R.id.tilProfessor)
        val tilHorario = view.findViewById<TextInputLayout>(R.id.tilHorario)
        val tilVagas = view.findViewById<TextInputLayout>(R.id.tilVagas)
        val etNomeAula = view.findViewById<TextInputEditText>(R.id.etNomeAula)
        val etProfessor = view.findViewById<MaterialAutoCompleteTextView>(R.id.etProfessor)
        val etHorario = view.findViewById<TextInputEditText>(R.id.etHorario)
        val etVagas = view.findViewById<TextInputEditText>(R.id.etVagas)

        // Lista suspensa com os professores ativos
        val professores = listaUsuarios.filter { it.professor && it.ativo }
        etProfessor.setSimpleItems(professores.map { it.nome }.toTypedArray())

        // Horário pelo relógio
        etHorario.setOnClickListener {
            TimePickerDialog(this, { _, hora, minuto ->
                etHorario.setText("%02d:%02d".format(hora, minuto))
                tilHorario.error = null
            }, 18, 30, true).show()
        }

        val painel = BottomSheetDialog(this)
        painel.setContentView(view)
        view.findViewById<View>(R.id.btnFecharSheet).setOnClickListener { painel.dismiss() } // RF23.6

        view.findViewById<MaterialButton>(R.id.btnSalvarAula).setOnClickListener {
            val nome = etNomeAula.text.toString().trim()
            val nomeProfessor = etProfessor.text.toString()
            val horario = etHorario.text.toString()
            val vagas = etVagas.text.toString().toIntOrNull()

            // Campos obrigatórios (RF23.4)
            tilNomeAula.error = if (nome.isEmpty()) "Digite o nome" else null
            tilProfessor.error = if (nomeProfessor.isEmpty()) "Escolha o professor" else null
            tilHorario.error = if (horario.isEmpty()) "Escolha" else null
            tilVagas.error = if (vagas == null || vagas <= 0) "Inválido" else null
            if (tilNomeAula.error != null || tilProfessor.error != null ||
                tilHorario.error != null || tilVagas.error != null
            ) return@setOnClickListener

            // "Prof." ou "Prof.ª" + primeiro nome, como nas outras aulas
            val professor = professores.first { it.nome == nomeProfessor }
            val titulo = if (professor.detalhe.startsWith("Professora")) "Prof.ª" else "Prof."
            val categoria = categoriasAulas.firstOrNull { it.equals(nome, ignoreCase = true) } ?: nome
            val dia = if (periodoSelecionado == 1) 1 else 0

            // Registra a aula e atualiza a lista (RF23.5)
            listaAulas.add(Aula(nome, categoria, "$titulo ${nomeProfessor.split(" ").first()}", dia, horario,
                "Sala 1", vagas!!, 0, false, iconeDaCategoria(categoria)))
            Toast.makeText(this, "Aula cadastrada", Toast.LENGTH_SHORT).show()
            painel.dismiss()
            mostrarAulas()
        }
        painel.show()
    }
}
