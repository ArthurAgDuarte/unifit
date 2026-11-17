package com.example.unifit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Tela "Exercícios e Aparelhos" do administrador (RF24, pág. 45)
class ExerciciosAparelhosActivity : TelaBase() {
    lateinit var btnVoltar: View
    lateinit var abas: TabLayout
    lateinit var tilBusca: TextInputLayout
    lateinit var etBusca: TextInputEditText
    lateinit var txtContagem: TextView
    lateinit var btnFiltrar: TextView
    lateinit var layoutItens: LinearLayout
    lateinit var txtSemItens: TextView
    lateinit var fabNovoItem: View

    var abaAparelhos = false // false = Exercícios, true = Aparelhos
    var filtro = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercicios_aparelhos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        abas = findViewById(R.id.abas)
        tilBusca = findViewById(R.id.tilBusca)
        etBusca = findViewById(R.id.etBusca)
        txtContagem = findViewById(R.id.txtContagem)
        btnFiltrar = findViewById(R.id.btnFiltrar)
        layoutItens = findViewById(R.id.layoutItens)
        txtSemItens = findViewById(R.id.txtSemItens)
        fabNovoItem = findViewById(R.id.fabNovoItem)

        // Ícone pequeno de filtro antes do texto "Filtrar"
        val iconeFiltro = getDrawable(R.drawable.ic_filtro)!!.mutate()
        val tamanho = (16 * resources.displayMetrics.density).toInt()
        iconeFiltro.setBounds(0, 0, tamanho, tamanho)
        iconeFiltro.setTint(getColor(R.color.azul_unifit))
        btnFiltrar.setCompoundDrawablesRelative(iconeFiltro, null, null, null)

        btnVoltar.setOnClickListener { finish() }
        etBusca.doAfterTextChanged { mostrarItens() }
        tilBusca.setEndIconOnClickListener { escolherFiltro() }
        btnFiltrar.setOnClickListener { escolherFiltro() }
        fabNovoItem.setOnClickListener { cadastrarItem() } // RF24.2: conforme a aba ativa

        // Troca de aba (RF24)
        abas.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                abaAparelhos = tab.position == 1
                filtro = "Todos"
                etBusca.setText("")
                etBusca.hint = if (abaAparelhos) "Buscar aparelho" else "Buscar exercício"
                mostrarItens()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        mostrarItens()
    }

    fun mostrarItens() {
        layoutItens.removeAllViews()
        val busca = etBusca.text.toString().trim()
        var quantidade = 0

        if (!abaAparelhos) {
            val filtrados = listaExerciciosCatalogo.filter {
                (filtro == "Todos" || it.grupo == filtro) && (busca.isEmpty() || it.nome.contains(busca, ignoreCase = true))
            }
            for (exercicio in filtrados) {
                val item = criarCard(exercicio.nome, "${exercicio.grupo} • ${exercicio.equipamento}", true)
                item.setOnClickListener {
                    // Detalhe do exercício, no modo catálogo (RF24.3)
                    val intent = Intent(this, DetalheExercicioActivity::class.java)
                    intent.putExtra("nomeExercicio", exercicio.nome)
                    intent.putExtra("musculos", "${exercicio.grupo} • ${exercicio.equipamento}")
                    startActivity(intent)
                }
                layoutItens.addView(item)
            }
            quantidade = filtrados.size
            txtContagem.text = "$quantidade exercícios"
        } else {
            val filtrados = listaAparelhos.filter {
                val passaFiltro = when (filtro) {
                    "Disponíveis" -> it.disponivel
                    "Em manutenção" -> !it.disponivel
                    else -> true
                }
                passaFiltro && (busca.isEmpty() || it.nome.contains(busca, ignoreCase = true))
            }
            for (aparelho in filtrados) {
                val status = if (aparelho.disponivel) "Disponível" else "Em manutenção"
                val item = criarCard(aparelho.nome, "${aparelho.area} • $status", false)
                // Detalhe do Aparelho (RF25) = tela "Detalhe do Espaço" do colega
                item.setOnClickListener { abrirTela(this, DetalheEspacoActivity::class.java) }
                layoutItens.addView(item)
            }
            quantidade = filtrados.size
            txtContagem.text = "$quantidade aparelhos"
        }
        btnFiltrar.text = if (filtro == "Todos") "Filtrar" else filtro
        txtSemItens.visibility = if (quantidade == 0) View.VISIBLE else View.GONE
    }

    fun criarCard(titulo: String, subtitulo: String, comFoto: Boolean): View {
        val item = layoutInflater.inflate(R.layout.item_card_lista, layoutItens, false)
        val imgItem = item.findViewById<ImageView>(R.id.imgItem)
        if (comFoto) {
            // Exercício: foto em vez de ícone (como no protótipo)
            imgItem.setPadding(0, 0, 0, 0)
            imgItem.setBackgroundResource(R.drawable.bg_miniatura)
            imgItem.clearColorFilter()
            imgItem.scaleType = ImageView.ScaleType.CENTER_CROP
            imgItem.setImageResource(R.drawable.foto_exercicio)
        } else {
            imgItem.setImageResource(R.drawable.ic_haltere)
        }
        item.findViewById<TextView>(R.id.txtTituloItem).text = titulo
        item.findViewById<TextView>(R.id.txtSubtituloItem).text = subtitulo
        return item
    }

    // Filtrar: grupo muscular (exercícios) ou status (aparelhos)
    fun escolherFiltro() {
        val opcoes = if (abaAparelhos) listOf("Todos", "Disponíveis", "Em manutenção") else listOf("Todos") + gruposMusculares
        val menu = PopupMenu(this, btnFiltrar)
        for (opcao in opcoes) menu.menu.add(opcao)
        menu.setOnMenuItemClickListener { item ->
            filtro = item.title.toString()
            mostrarItens()
            true
        }
        menu.show()
    }

    // Botão "+": cadastra exercício ou aparelho conforme a aba (RF24.2)
    fun cadastrarItem() {
        if (!abaAparelhos) {
            mostrarDialogoNovoItem(this, "Novo exercício", "Nome do exercício", "Equipamento", "Grupo muscular",
                gruposMusculares
            ) { nome, equipamento, grupo ->
                listaExerciciosCatalogo.add(0, ExercicioCatalogo(nome, grupo, equipamento))
                Toast.makeText(this, "Exercício cadastrado", Toast.LENGTH_SHORT).show()
                mostrarItens()
            }
        } else {
            mostrarDialogoNovoItem(this, "Novo aparelho", "Nome do aparelho", "Área / sala", "Status",
                arrayOf("Disponível", "Em manutenção")
            ) { nome, area, status ->
                listaAparelhos.add(0, Aparelho(nome, area, status == "Disponível"))
                Toast.makeText(this, "Aparelho cadastrado", Toast.LENGTH_SHORT).show()
                mostrarItens()
            }
        }
    }
}
