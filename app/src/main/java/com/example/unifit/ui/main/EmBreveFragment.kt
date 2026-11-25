package com.example.unifit.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.unifit.databinding.FragmentEmBreveBinding

// Placeholder das abas fora do escopo (Início, Aulas, Mapa)
class EmBreveFragment : Fragment() {

    private var _binding: FragmentEmBreveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmBreveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // a View morre antes do Fragment: evita vazamento
    }
}
