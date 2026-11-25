package com.example.unifit.ui.detalhetreino

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifit.R
import com.example.unifit.databinding.ItemExercicioBinding
import com.example.unifit.model.ItemExercicio
import com.example.unifit.util.Formatos
import com.example.unifit.util.Fotos

// Uma linha da lista: o exercício + se já foi concluído hoje
data class ExercicioLinha(
    val exercicio: ItemExercicio,
    val concluido: Boolean
)

class ExercicioAdapter(
    private val aoTocar: (ItemExercicio) -> Unit,
    private val aoTocarPlay: (ItemExercicio) -> Unit
) : ListAdapter<ExercicioLinha, ExercicioAdapter.ExercicioViewHolder>(Diferenca) {

    inner class ExercicioViewHolder(private val binding: ItemExercicioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(linha: ExercicioLinha) {
            val exercicio = linha.exercicio
            val contexto = binding.root.context

            binding.imgExercicio.setImageResource(Fotos.doExercicio())
            binding.txtNomeExercicio.text = exercicio.nome
            // "4 × 10 • 60 kg"
            binding.txtSeriesExercicio.text = contexto.getString(
                R.string.exercicio_series,
                exercicio.series,
                exercicio.repeticoes,
                Formatos.carga(contexto, exercicio.cargaKg)
            )
            binding.imgConcluido.visibility = if (linha.concluido) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { aoTocar(exercicio) }
            binding.btnPlay.setOnClickListener { aoTocarPlay(exercicio) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioViewHolder {
        val binding = ItemExercicioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExercicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExercicioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object Diferenca : DiffUtil.ItemCallback<ExercicioLinha>() {
        override fun areItemsTheSame(antigo: ExercicioLinha, novo: ExercicioLinha) =
            antigo.exercicio.exercicioId == novo.exercicio.exercicioId
        override fun areContentsTheSame(antigo: ExercicioLinha, novo: ExercicioLinha) = antigo == novo
    }
}
