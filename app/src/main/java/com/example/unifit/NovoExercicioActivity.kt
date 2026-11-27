package com.example.unifit

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Tela "Novo Exercício" (RF24.2, pág. 45)
class NovoExercicioActivity : TelaBase() {

    lateinit var btnVoltar: View
    lateinit var tilNome: TextInputLayout
    lateinit var etNome: TextInputEditText
    lateinit var tilGrupo: TextInputLayout
    lateinit var edtGrupo: MaterialAutoCompleteTextView
    lateinit var tilAparelho: TextInputLayout
    lateinit var edtAparelho: MaterialAutoCompleteTextView
    lateinit var layoutUploadVideo: LinearLayout
    lateinit var txtUploadTitle: TextView
    lateinit var txtQtdPassos: TextView
    lateinit var layoutPassos: LinearLayout
    lateinit var btnAdicionarPasso: TextView
    lateinit var btnSalvar: MaterialButton

    val listaPassos = mutableListOf(
        "Deite no banco com os pés firmes no chão.",
        "Segure a barra um pouco além da largura dos ombros.",
        "Desça a barra até o peito e empurre de volta."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_novo_exercicio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        tilNome = findViewById(R.id.tilNome)
        etNome = findViewById(R.id.etNome)
        tilGrupo = findViewById(R.id.tilGrupo)
        edtGrupo = findViewById(R.id.edtGrupo)
        tilAparelho = findViewById(R.id.tilAparelho)
        edtAparelho = findViewById(R.id.edtAparelho)
        layoutUploadVideo = findViewById(R.id.layoutUploadVideo)
        txtUploadTitle = findViewById(R.id.txtUploadTitle)
        txtQtdPassos = findViewById(R.id.txtQtdPassos)
        layoutPassos = findViewById(R.id.layoutPassos)
        btnAdicionarPasso = findViewById(R.id.btnAdicionarPasso)
        btnSalvar = findViewById(R.id.btnSalvar)

        etNome.doAfterTextChanged { tilNome.error = null }
        btnVoltar.setOnClickListener { finish() }

        // Configura as opções dos menus suspensos
        edtGrupo.setSimpleItems(gruposMusculares)
        edtAparelho.setSimpleItems(listaAparelhos.map { it.nome }.toTypedArray())

        layoutUploadVideo.setOnClickListener {
            // Seletor de vídeo demonstrativo
            txtUploadTitle.text = "Vídeo selecionado ✓"
            Toast.makeText(this, "Vídeo demonstrativo selecionado", Toast.LENGTH_SHORT).show()
        }

        btnAdicionarPasso.setOnClickListener {
            listaPassos.add("")
            renderizarPassos()
        }

        btnSalvar.setOnClickListener { salvarExercicio() }

        renderizarPassos()
    }

    fun renderizarPassos() {
        layoutPassos.removeAllViews()
        val qtd = listaPassos.size
        txtQtdPassos.text = if (qtd == 1) "1 passo" else "$qtd passos"

        for (i in listaPassos.indices) {
            val itemPasso = layoutInflater.inflate(R.layout.item_passo_editavel, layoutPassos, false)
            itemPasso.findViewById<TextView>(R.id.txtNumeroPasso).text = (i + 1).toString()

            val edtPasso = itemPasso.findViewById<EditText>(R.id.edtPassoText)
            edtPasso.setText(listaPassos[i])
            edtPasso.doAfterTextChanged { texto ->
                listaPassos[i] = texto.toString()
            }

            layoutPassos.addView(itemPasso)
        }
    }

    fun salvarExercicio() {
        val nome = etNome.text.toString().trim()
        val grupo = edtGrupo.text.toString().trim()
        val aparelho = edtAparelho.text.toString().trim()

        tilNome.error = if (nome.isEmpty()) "Informe o nome do exercício" else null
        if (tilNome.error != null) return

        val grupoFinal = if (grupo.isNotEmpty()) grupo else "Peitoral"
        val equipamentoFinal = if (aparelho.isNotEmpty()) aparelho else "Barra livre"

        // Cadastra o exercício no catálogo (RF24.2)
        listaExerciciosCatalogo.add(0, ExercicioCatalogo(nome, grupoFinal, equipamentoFinal))

        Toast.makeText(this, "Exercício cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}