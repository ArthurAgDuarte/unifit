package com.example.unifit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RecuperarSenhaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_senha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val btnSendCode = findViewById<MaterialButton>(R.id.btnSendCode)

        intent.getStringExtra(EXTRA_EMAIL)?.let { receivedEmail ->
            etEmail.setText(receivedEmail)
        }

        btnSendCode.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                tilEmail.error = "Por favor, digite seu e-mail"
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "E-mail inválido"
            } else {
                tilEmail.error = null
                Toast.makeText(this, "Código enviado com sucesso para $email!", Toast.LENGTH_LONG).show()
                // Depois de 5 segundos volta para o Login (que está logo abaixo desta tela)
                btnSendCode.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 5000)
            }
        }
    }
}
