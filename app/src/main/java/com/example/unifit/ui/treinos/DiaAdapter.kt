package com.example.unifit.ui.treinos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifit.R
import com.example.unifit.databinding.ItemDiaBinding
import com.example.unifit.util.Tema
import java.time.LocalDate

// Um dia do seletor: a data e como ele deve aparecer
data class DiaSemana(
    val data: LocalDate,
    val selecionado: Boolean,
    val hoje: Boolean
)

class DiaAdapter(
    private val aoTocar: (LocalDate) -> Unit
) : ListAdapter<DiaSemana, DiaAdapter.DiaViewHolder>(Diferenca) {

    class DiaViewHolder(
        private val binding: ItemDiaBinding,
        private val aoTocar: (LocalDate) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dia: DiaSemana) {
            val contexto = binding.root.context
            val indice = dia.data.dayOfWeek.value - 1 // DayOfWeek: segunda = 1 ... domingo = 7

            binding.txtNomeDia.text = contexto.resources.getStringArray(R.array.dias_semana_sigla)[indice]
            binding.txtNumeroDia.text = dia.data.dayOfMonth.toString()
            binding.root.contentDescription = contexto.getString(
                R.string.dia_descricao,
                contexto.resources.getStringArray(R.array.dias_semana_completo)[indice],
                dia.data.dayOfMonth
            )
            binding.root.isSelected = dia.selecionado

            // Selecionado: fundo de destaque | hoje (não selecionado): texto de destaque | demais: padrão.
            // As cores vêm do tema (R.attr), então seguem o alto contraste.
            val corSigla: Int
            val corNumero: Int
            when {
                dia.selecionado -> {
                    binding.root.setBackgroundResource(R.drawable.bg_dia_selecionado)
                    corSigla = R.attr.corTextoSobreDestaque
                    corNumero = R.attr.corTextoSobreDestaque
                }
                dia.hoje -> {
                    binding.root.setBackgroundResource(R.drawable.bg_dia_normal)
                    corSigla = R.attr.corDestaque
                    corNumero = R.attr.corDestaque
                }
                else -> {
                    binding.root.setBackgroundResource(R.drawable.bg_dia_normal)
                    corSigla = R.attr.corTextoSecundario
                    corNumero = R.attr.corTextoPrincipal
                }
            }
            binding.txtNomeDia.setTextColor(Tema.cor(contexto, corSigla))
            binding.txtNumeroDia.setTextColor(Tema.cor(contexto, corNumero))

            binding.root.setOnClickListener { aoTocar(dia.data) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val binding = ItemDiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaViewHolder(binding, aoTocar)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // O DiffUtil só redesenha os dias que mudaram (o antigo e o novo selecionado)
    private object Diferenca : DiffUtil.ItemCallback<DiaSemana>() {
        override fun areItemsTheSame(antigo: DiaSemana, novo: DiaSemana) = antigo.data == novo.data
        override fun areContentsTheSame(antigo: DiaSemana, novo: DiaSemana) = antigo == novo
    }
}
