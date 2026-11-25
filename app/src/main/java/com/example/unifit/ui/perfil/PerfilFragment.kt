package com.example.unifit.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.unifit.ui.acessibilidade.AcessibilidadeActivity
import com.example.unifit.databinding.FragmentPerfilBinding

// Aba Perfil: por enquanto só o atalho para Acessibilidade
class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configurações de acessibilidade
        binding.opcaoAcessibilidade.setOnClickListener {
            startActivity(Intent(requireContext(), AcessibilidadeActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
