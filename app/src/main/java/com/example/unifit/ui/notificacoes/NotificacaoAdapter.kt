package com.example.unifit.ui.notificacoes

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.unifit.R
import com.example.unifit.databinding.ItemCabecalhoDataBinding
import com.example.unifit.databinding.ItemNotificacaoBinding
import com.example.unifit.model.Notificacao
import com.example.unifit.util.Constantes
import com.example.unifit.util.Tema

// Cada linha da lista é um cabeçalho de data OU uma notificação
sealed interface LinhaNotificacao {
    data class Cabecalho(val rotulo: String) : LinhaNotificacao
    data class Item(val notificacao: Notificacao, val tempo: String) : LinhaNotificacao
}

// RecyclerView com 2 viewTypes: cabeçalho de data e notificação
class NotificacaoAdapter(
    private val aoTocar: (Notificacao) -> Unit
) : ListAdapter<LinhaNotificacao, RecyclerView.ViewHolder>(Diferenca) {

    class CabecalhoViewHolder(private val binding: ItemCabecalhoDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(linha: LinhaNotificacao.Cabecalho) {
            binding.txtCabecalhoData.text = linha.rotulo
        }
    }

    class NotificacaoViewHolder(
        private val binding: ItemNotificacaoBinding,
        private val aoTocar: (Notificacao) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(linha: LinhaNotificacao.Item) {
            val notificacao = linha.notificacao
            val contexto = binding.root.context

            binding.imgNotificacao.setImageResource(iconeDa(notificacao.categoria))
            binding.txtTituloNotificacao.text = notificacao.titulo
            binding.txtDetalheNotificacao.text =
                contexto.getString(R.string.notificacao_detalhe, notificacao.descricao, linha.tempo)

            // Os dois estados são definidos sempre, porque o ViewHolder é reaproveitado entre itens
            if (notificacao.lida) {
                binding.root.setBackgroundResource(R.drawable.bg_card_lido)
                binding.imgNotificacao.backgroundTintList =
                    ColorStateList.valueOf(Tema.cor(contexto, R.attr.corIndicadorInativo))
                binding.imgNotificacao.imageTintList =
                    ColorStateList.valueOf(Tema.cor(contexto, R.attr.corTextoSecundario))
                binding.pontoNaoLida.visibility = View.GONE
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_card_tema)
                binding.imgNotificacao.backgroundTintList =
                    ColorStateList.valueOf(Tema.cor(contexto, R.attr.corDestaqueClaro))
                binding.imgNotificacao.imageTintList =
                    ColorStateList.valueOf(Tema.cor(contexto, R.attr.corDestaque))
                binding.pontoNaoLida.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener { aoTocar(notificacao) }
        }

        @DrawableRes
        private fun iconeDa(categoria: String): Int = when (categoria) {
            Constantes.CATEGORIA_AULA_CONFIRMADA -> R.drawable.ic_calendario_contorno
            Constantes.CATEGORIA_AULA_CANCELADA -> R.drawable.ic_fechar
            Constantes.CATEGORIA_HIDRATACAO -> R.drawable.ic_gota
            Constantes.CATEGORIA_FICHA_ATUALIZADA -> R.drawable.ic_haltere
            Constantes.CATEGORIA_CONQUISTA -> R.drawable.ic_trofeu
            else -> R.drawable.ic_notificacao
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is LinhaNotificacao.Cabecalho -> TIPO_CABECALHO
        is LinhaNotificacao.Item -> TIPO_NOTIFICACAO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_CABECALHO) {
            CabecalhoViewHolder(ItemCabecalhoDataBinding.inflate(inflater, parent, false))
        } else {
            NotificacaoViewHolder(ItemNotificacaoBinding.inflate(inflater, parent, false), aoTocar)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val linha = getItem(position)) {
            is LinhaNotificacao.Cabecalho -> (holder as CabecalhoViewHolder).bind(linha)
            is LinhaNotificacao.Item -> (holder as NotificacaoViewHolder).bind(linha)
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<LinhaNotificacao>() {
        override fun areItemsTheSame(antigo: LinhaNotificacao, novo: LinhaNotificacao): Boolean = when {
            antigo is LinhaNotificacao.Cabecalho && novo is LinhaNotificacao.Cabecalho -> antigo.rotulo == novo.rotulo
            antigo is LinhaNotificacao.Item && novo is LinhaNotificacao.Item -> antigo.notificacao.id == novo.notificacao.id
            else -> false
        }

        override fun areContentsTheSame(antigo: LinhaNotificacao, novo: LinhaNotificacao) = antigo == novo
    }

    companion object {
        private const val TIPO_CABECALHO = 0
        private const val TIPO_NOTIFICACAO = 1
    }
}
