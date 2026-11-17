package com.example.unifit

import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

// Pedaços repetidos nas telas novas (chips de filtro, datas e números)

val localeBrasil: Locale = Locale.forLanguageTag("pt-BR")
val diasDaSemana = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
val mesesCurtos = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
val mesesLongos = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto",
    "setembro", "outubro", "novembro", "dezembro")

// Monta uma fileira de chips (Todas / Spinning / Yoga...) e marca o selecionado
fun montarChips(
    activity: AppCompatActivity,
    layout: LinearLayout,
    opcoes: List<String>,
    selecionado: Int,
    aoEscolher: (Int) -> Unit
) {
    layout.removeAllViews()
    for (i in opcoes.indices) {
        val chip = activity.layoutInflater.inflate(R.layout.item_chip, layout, false) as TextView
        chip.text = opcoes[i]
        if (i == selecionado) {
            chip.setBackgroundResource(R.drawable.bg_chip_selecionado)
            chip.setTextColor(activity.getColor(R.color.white))
        }
        chip.setOnClickListener { aoEscolher(i) }
        layout.addView(chip)
    }
}

// 1840.0 -> "1.840" | 64.2 -> "64,2" | 1.6 -> "1,6"
fun formatarNumero(valor: Double): String {
    val formato = NumberFormat.getInstance(localeBrasil)
    formato.maximumFractionDigits = 1
    return formato.format(valor)
}

// "Hoje", "Amanhã" ou "Qui, 29/10"
fun nomeDoDia(diaOffset: Int): String {
    val data = LocalDate.now().plusDays(diaOffset.toLong())
    return when (diaOffset) {
        0 -> "Hoje"
        1 -> "Amanhã"
        else -> "${diasDaSemana[data.dayOfWeek.value - 1]}, %02d/%02d".format(data.dayOfMonth, data.monthValue)
    }
}

// "16 de setembro"
fun dataPorExtenso(data: LocalDate): String = "${data.dayOfMonth} de ${mesesLongos[data.monthValue - 1]}"

// "16 set"
fun dataCurta(data: LocalDate): String = "${data.dayOfMonth} ${mesesCurtos[data.monthValue - 1]}"

// Preenche um item_card_lista com os dados de uma aula.
// visaoAdmin = true mostra "16/20 vagas" (Gerenciar Aulas); false mostra "4 vagas" (Aulas Coletivas)
fun preencherCardAula(activity: AppCompatActivity, item: android.view.View, aula: Aula, visaoAdmin: Boolean) {
    val imgItem = item.findViewById<android.widget.ImageView>(R.id.imgItem)
    imgItem.setImageResource(aula.icone)
    item.findViewById<TextView>(R.id.txtTituloItem).text = aula.nome

    val vagas = when {
        visaoAdmin -> "${aula.inscritos}/${aula.vagas} vagas"
        aula.lotada() -> "Lotada"
        aula.vagasRestantes() == 1 -> "1 vaga"
        else -> "${aula.vagasRestantes()} vagas"
    }
    val reservada = if (aula.reservada && !visaoAdmin) " • Reservada" else ""
    item.findViewById<TextView>(R.id.txtSubtituloItem).text = "${aula.horario} • ${aula.professor} • $vagas$reservada"

    // Aula lotada: ícone vermelho (como a de Funcional no protótipo)
    if (aula.lotada()) {
        imgItem.setBackgroundResource(R.drawable.bg_icone_vermelho)
        imgItem.setColorFilter(activity.getColor(R.color.vermelho_status))
    }
}
