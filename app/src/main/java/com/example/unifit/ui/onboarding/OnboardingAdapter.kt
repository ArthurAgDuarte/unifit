package com.example.unifit.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unifit.databinding.ItemOnboardingBinding

// Adapter do ViewPager2: a lista de slides é fixa, então um Adapter simples basta
class OnboardingAdapter(
    private val slides: List<SlideOnboarding>
) : RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder>() {

    class SlideViewHolder(private val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(slide: SlideOnboarding) {
            binding.imgIlustracao.setImageResource(slide.icone)
            binding.txtTituloOnboarding.setText(slide.titulo)
            binding.txtDescricao.setText(slide.descricao)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.bind(slides[position])
    }

    override fun getItemCount(): Int = slides.size
}
