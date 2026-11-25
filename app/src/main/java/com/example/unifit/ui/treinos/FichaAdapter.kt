package com.example.unifit.ui.treinos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifit.R
import com.example.unifit.databinding.ItemFichaBinding
import com.example.unifit.model.Ficha

// Uma linha da lista: a ficha + o que depende da tela (etiqueta "Hoje" e foto pela posição)
data class FichaItem(
    val ficha: Ficha,
    val hoje: Boolean,
    @DrawableRes val foto: Int
)

class FichaAdapter(
    private val aoTocar: (Ficha) -> Unit
) : ListAdapter<FichaItem, FichaAdapter.FichaViewHolder>(Diferenca) {

    class FichaViewHolder(
        private val binding: ItemFichaBinding,
        private val aoTocar: (Ficha) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FichaItem) {
            val ficha = item.ficha
            val recursos = binding.root.resources

            binding.imgFicha.setImageResource(item.foto)
            binding.txtNomeFicha.text = ficha.nome
            // "8 exercícios • ~45 min"
            val exercicios = recursos.getQuantityString(
                R.plurals.ficha_qtd_exercicios, ficha.exercicios.size, ficha.exercicios.size
            )
            binding.txtInfoFicha.text = recursos.getString(R.string.ficha_info, exercicios, ficha.duracaoMin)
            binding.txtHoje.visibility = if (item.hoje) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { aoTocar(ficha) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FichaViewHolder {
        val binding = ItemFichaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FichaViewHolder(binding, aoTocar)
    }

    override fun onBindViewHolder(holder: FichaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object Diferenca : DiffUtil.ItemCallback<FichaItem>() {
        override fun areItemsTheSame(antigo: FichaItem, novo: FichaItem) = antigo.ficha.id == novo.ficha.id
        override fun areContentsTheSame(antigo: FichaItem, novo: FichaItem) = antigo == novo
    }
}
