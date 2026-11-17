package com.example.unifit

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.unifit.databinding.ActivitySignUpBinding
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTexts()
        setupErrorClearing()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCreate.setOnClickListener { validateAndSubmit() }
    }

    private fun setupErrorClearing() {
        fun TextInputLayout.clearErrorOnType(edit: EditText) {
            edit.doAfterTextChanged { error = null }
        }

        binding.tilName.clearErrorOnType(binding.etName)
        binding.tilEmail.clearErrorOnType(binding.etEmail)
        binding.tilPassword.clearErrorOnType(binding.etPassword)
        binding.tilConfirm.clearErrorOnType(binding.etConfirm)
    }

    private fun setupTexts() {
        binding.txtTerms.setLinkedText(
            fullText = "Li e aceito os Termos de Uso e a Política de Privacidade do UniFit.",
            links = mapOf(
                "Termos de Uso" to { },
                "Política de Privacidade" to { }
            )
        )
        binding.txtLogin.setLinkedText(
            fullText = "Já tem conta? Entrar",
            links = mapOf("Entrar" to { finish() })
        )
    }

    private fun validateAndSubmit() = with(binding) {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString()
        val confirm = etConfirm.text.toString()

        tilName.error = if (name.isEmpty()) "Informe seu nome" else null

        tilEmail.error = when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "E-mail inválido"
            !email.endsWith(".edu.br") -> "Use seu e-mail institucional (.edu.br)"
            else -> null
        }

        tilPassword.error = if (pass.length < 8) "Mínimo de 8 caracteres" else null
        tilConfirm.error = if (confirm != pass) "As senhas não coincidem" else null

        val fieldsValid = listOf(tilName, tilEmail, tilPassword, tilConfirm).all { it.error == null }

        if (!cbTerms.isChecked) {
            Toast.makeText(
                this@SignUpActivity,
                "Aceite os Termos de Uso para continuar",
                Toast.LENGTH_SHORT
            ).show()
            return@with
        }

        if (fieldsValid) {
            // aqui entra o cadastro de verdade; por enquanto só vai para o Início
            abrirTelaLimpandoHistorico(this@SignUpActivity, HomeActivity::class.java)
        }
    }
}

fun TextView.setLinkedText(fullText: String, links: Map<String, () -> Unit>) {
    val color = ContextCompat.getColor(context, R.color.primary)
    val spannable = SpannableString(fullText)

    links.forEach { (label, action) ->
        val start = fullText.indexOf(label)
        if (start < 0) return@forEach
        val end = start + label.length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) = action()
            override fun updateDrawState(ds: TextPaint) {
                ds.color = color
                ds.isUnderlineText = false
                ds.typeface = Typeface.DEFAULT_BOLD
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    text = spannable
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}