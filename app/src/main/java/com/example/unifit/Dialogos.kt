package com.example.unifit

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// Diálogo para criar uma ficha nova (ficha = null) ou editar uma existente
fun mostrarDialogoFicha(activity: AppCompatActivity, ficha: Ficha?, aoSalvar: () -> Unit) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_ficha, null)
    val campoNome = view.findViewById<TextInputLayout>(R.id.campoNome)
    val campoDuracao = view.findViewById<TextInputLayout>(R.id.campoDuracao)
    val edtNome = view.findViewById<TextInputEditText>(R.id.edtNome)
    val edtDuracao = view.findViewById<TextInputEditText>(R.id.edtDuracao)
    val edtNivel = view.findViewById<MaterialAutoCompleteTextView>(R.id.edtNivel)

    edtNivel.setSimpleItems(niveis)
    if (ficha != null) {
        edtNome.setText(ficha.nome)
        edtDuracao.setText(ficha.duracaoMin.toString())
        edtNivel.setText(ficha.nivel, false)
    } else {
        edtNivel.setText(niveis[0], false)
    }

    val dialogo = MaterialAlertDialogBuilder(activity)
        .setTitle(if (ficha == null) "Nova ficha" else "Editar ficha")
        .setView(view)
        .setNegativeButton("Cancelar", null)
        .setPositiveButton("Salvar", null)
        .show()

    // O clique é tratado aqui para o diálogo não fechar se tiver campo vazio
    dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val nome = edtNome.text.toString().trim()
        val duracao = edtDuracao.text.toString().toIntOrNull()

        campoNome.error = if (nome.isEmpty()) "Digite um nome" else null
        campoDuracao.error = if (duracao == null || duracao <= 0) "Digite a duração" else null
        if (campoNome.error != null || campoDuracao.error != null) return@setOnClickListener

        if (ficha == null) {
            listaFichas.add(Ficha(nome, duracao!!, edtNivel.text.toString(), R.drawable.foto_padrao, mutableListOf()))
        } else {
            ficha.nome = nome
            ficha.duracaoMin = duracao!!
            ficha.nivel = edtNivel.text.toString()
        }
        aoSalvar()
        dialogo.dismiss()
    }
}

// Diálogo para adicionar um exercício a uma ficha
fun mostrarDialogoExercicio(activity: AppCompatActivity, ficha: Ficha, aoSalvar: () -> Unit) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_exercicio, null)
    val campoNome = view.findViewById<TextInputLayout>(R.id.campoNomeExercicio)
    val campoSeries = view.findViewById<TextInputLayout>(R.id.campoSeries)
    val campoRepeticoes = view.findViewById<TextInputLayout>(R.id.campoRepeticoes)
    val campoCarga = view.findViewById<TextInputLayout>(R.id.campoCarga)
    val edtNome = view.findViewById<TextInputEditText>(R.id.edtNomeExercicio)
    val edtSeries = view.findViewById<TextInputEditText>(R.id.edtSeries)
    val edtRepeticoes = view.findViewById<TextInputEditText>(R.id.edtRepeticoes)
    val edtCarga = view.findViewById<TextInputEditText>(R.id.edtCarga)

    val dialogo = MaterialAlertDialogBuilder(activity)
        .setTitle("Novo exercício")
        .setView(view)
        .setNegativeButton("Cancelar", null)
        .setPositiveButton("Adicionar", null)
        .show()

    dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val nome = edtNome.text.toString().trim()
        val series = edtSeries.text.toString().toIntOrNull()
        val repeticoes = edtRepeticoes.text.toString().toIntOrNull()
        val carga = edtCarga.text.toString().trim()

        campoNome.error = if (nome.isEmpty()) "Digite um nome" else null
        campoSeries.error = if (series == null || series <= 0) "Inválido" else null
        campoRepeticoes.error = if (repeticoes == null || repeticoes <= 0) "Inválido" else null
        campoCarga.error = if (carga.isEmpty()) "Digite a carga" else null
        if (campoNome.error != null || campoSeries.error != null ||
            campoRepeticoes.error != null || campoCarga.error != null
        ) return@setOnClickListener

        ficha.exercicios.add(Exercicio(nome, series!!, repeticoes!!, carga))
        aoSalvar()
        dialogo.dismiss()
    }
}

// Abre um link no app certo (YouTube, Maps...) ou no navegador
fun abrirLink(activity: AppCompatActivity, link: String) {
    try {
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, "Nenhum app encontrado para abrir o link", Toast.LENGTH_SHORT).show()
    }
}

// Formulário genérico de cadastro com 2 campos de texto e 1 lista suspensa
// (novo usuário, novo exercício e novo aparelho)
fun mostrarDialogoNovoItem(
    activity: AppCompatActivity,
    titulo: String,
    rotulo1: String,
    rotulo2: String,
    rotulo3: String,
    opcoes3: Array<String>,
    aoSalvar: (String, String, String) -> Unit
) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_novo_item, null)
    val tilCampo1 = view.findViewById<TextInputLayout>(R.id.tilCampo1)
    val tilCampo2 = view.findViewById<TextInputLayout>(R.id.tilCampo2)
    val tilCampo3 = view.findViewById<TextInputLayout>(R.id.tilCampo3)
    val etCampo1 = view.findViewById<TextInputEditText>(R.id.etCampo1)
    val etCampo2 = view.findViewById<TextInputEditText>(R.id.etCampo2)
    val etCampo3 = view.findViewById<MaterialAutoCompleteTextView>(R.id.etCampo3)

    tilCampo1.hint = rotulo1
    tilCampo2.hint = rotulo2
    tilCampo3.hint = rotulo3
    etCampo3.setSimpleItems(opcoes3)
    etCampo3.setText(opcoes3[0], false)

    val dialogo = MaterialAlertDialogBuilder(activity)
        .setTitle(titulo)
        .setView(view)
        .setNegativeButton("Cancelar", null)
        .setPositiveButton("Salvar", null)
        .show()

    // Só fecha se os campos estiverem preenchidos
    dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val valor1 = etCampo1.text.toString().trim()
        val valor2 = etCampo2.text.toString().trim()
        tilCampo1.error = if (valor1.isEmpty()) "Campo obrigatório" else null
        tilCampo2.error = if (valor2.isEmpty()) "Campo obrigatório" else null
        if (tilCampo1.error != null || tilCampo2.error != null) return@setOnClickListener

        aoSalvar(valor1, valor2, etCampo3.text.toString())
        dialogo.dismiss()
    }
}
