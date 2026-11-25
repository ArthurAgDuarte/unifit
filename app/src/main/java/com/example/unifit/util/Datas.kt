package com.example.unifit.util

import android.content.Context
import android.text.format.DateUtils
import com.example.unifit.R
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Funções de data usadas pelos repositórios e telas
object Datas {

    private val formatoDiaMes: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

    // Id do documento de progresso de um dia: "yyyy-MM-dd" (ex.: 2026-11-22)
    fun idDoDia(data: LocalDate): String = data.toString()

    fun idDeHoje(): String = idDoDia(LocalDate.now())

    // Timestamp do Firestore -> data no fuso do aparelho
    fun dataLocal(timestamp: Timestamp): LocalDate =
        timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    // Cabeçalho da lista de notificações: "Hoje", "Ontem" ou "15/09"
    fun rotuloDoDia(context: Context, data: LocalDate): String {
        val hoje = LocalDate.now()
        return when (data) {
            hoje -> context.getString(R.string.hoje)
            hoje.minusDays(1) -> context.getString(R.string.ontem)
            else -> data.format(formatoDiaMes)
        }
    }

    // "há 10 min", "há 2 h"... (o texto vem do Android, no idioma do aparelho)
    fun tempoRelativo(context: Context, timestamp: Timestamp): String {
        val agora = System.currentTimeMillis()
        val momento = timestamp.toDate().time
        if (agora - momento < DateUtils.MINUTE_IN_MILLIS) return context.getString(R.string.agora)
        return DateUtils.getRelativeTimeSpanString(
            momento,
            agora,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}
