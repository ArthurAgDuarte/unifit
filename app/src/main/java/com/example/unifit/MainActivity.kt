package com.example.unifit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge

class MainActivity : TelaBase() {
    lateinit var progressLoading: ProgressBar
    lateinit var txtCarregando: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        progressLoading = findViewById(R.id.progressLoading)
        txtCarregando = findViewById(R.id.txtCarregando)

        // Só começa a contar depois que o splash aparece na tela
        window.decorView.post { carregar() }
    }

    fun carregar() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3 segundos
    }
}
