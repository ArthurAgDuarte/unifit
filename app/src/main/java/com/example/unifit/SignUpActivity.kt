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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.unifit.data.FirebaseHelper
import com.example.unifit.databinding.ActivitySignUpBinding
import com.example.unifit.util.Constantes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue

// Tela "Criar Conta" (RF04, pág. 11 do PDF)
class SignUpActivity : TelaBase() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTexts()
        setupErrorClearing()

        binding.btnBack.setOnClickListener { finish() } // RF04.5: Retornar à tela anterior
        binding.btnCreate.setOnClickListener { validateAndSubmit() }
    }

    private fun setupErrorClearing() {
        fun TextInputLayout.clearErrorOnType(edit: EditText) {
            edit.doAfterTextChanged { error = null }
        }

        binding.tilName.clearErrorOnType(binding.etName)
        binding.tilEmail.clearErrorOnType(binding.etEmail)
        binding.tilMatricula.clearErrorOnType(binding.etMatricula)
        binding.tilPassword.clearErrorOnType(binding.etPassword)
        binding.tilConfirm.clearErrorOnType(binding.etConfirm)
    }

    private fun setupTexts() {
        // RF04.4: Links de acesso aos Termos de Uso e Política de Privacidade
        binding.txtTerms.setLinkedText(
            fullText = "Li e aceito os Termos de Uso e a Política de Privacidade do UniFit.",
            links = mapOf(
                "Termos de Uso" to { exibirDialogoTermos() },
                "Política de Privacidade" to { exibirDialogoPrivacidade() }
            )
        )

        // RF04.6: Redirecionar para a tela de Login
        binding.txtLogin.setLinkedText(
            fullText = "Já tem conta? Entrar",
            links = mapOf("Entrar" to {
                abrirTela(this, LoginActivity::class.java)
                finish()
            })
        )
    }

    private fun exibirDialogoTermos() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Termos de Uso")
            .setMessage("Ao utilizar o UniFit, você concorda em utilizar sua conta de forma pessoal e intransferível para acompanhamento de treinos e reservas de aulas no campus da universidade.")
            .setPositiveButton("Entendi", null)
            .show()
    }

    private fun exibirDialogoPrivacidade() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Política de Privacidade")
            .setMessage("Em conformidade com a LGPD (RNF03), os seus dados pessoais (nome, e-mail institucional e matrícula) são utilizados exclusivamente para autenticação e gestão do seu acesso aos serviços da academia.")
            .setPositiveButton("Entendi", null)
            .show()
    }

    private fun validateAndSubmit() = with(binding) {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val matricula = etMatricula.text.toString().trim()
        val pass = etPassword.text.toString()
        val confirm = etConfirm.text.toString()

        tilName.error = if (name.isEmpty()) "Informe seu nome completo" else null

        // RF04.1: Validar e-mail institucional (.edu.br ou universidade)
        tilEmail.error = when {
            email.isEmpty() -> "Informe seu e-mail institucional"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "E-mail inválido"
            !email.endsWith(".edu.br") && !email.contains("unifor.br") -> "Use seu e-mail institucional (.edu.br)"
            else -> null
        }

        tilMatricula.error = if (matricula.isEmpty()) "Informe sua matrícula" else null

        // RF04.2: Validar senha com no mínimo 8 caracteres e coincidência
        tilPassword.error = if (pass.length < 8) "A senha deve ter no mínimo 8 caracteres" else null
        tilConfirm.error = if (confirm != pass) "As senhas não coincidem" else null

        val fieldsValid = listOf(tilName, tilEmail, tilMatricula, tilPassword, tilConfirm).all { it.error == null }

        // RF04.3: Exigir marcação do checkbox de Termos de Uso
        if (!cbTerms.isChecked) {
            Toast.makeText(
                this@SignUpActivity,
                "Aceite os Termos de Uso para continuar",
                Toast.LENGTH_SHORT
            ).show()
            return@with
        }

        if (fieldsValid) {
            btnCreate.isEnabled = false

            FirebaseHelper.auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val uid = user?.uid

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)

                    val dadosUsuario = hashMapOf(
                        "nome" to name,
                        "email" to email,
                        "matricula" to matricula,
                        "criadoEm" to FieldValue.serverTimestamp()
                    )

                    if (uid != null) {
                        FirebaseHelper.db.collection(Constantes.COLECAO_USERS)
                            .document(uid)
                            .set(dadosUsuario)
                    }

                    nomeUsuario = name
                    emailUsuario = email

                    Toast.makeText(
                        this@SignUpActivity,
                        "Conta criada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    abrirTelaLimpandoHistorico(this@SignUpActivity, HomeActivity::class.java)
                }
                .addOnFailureListener { exception ->
                    btnCreate.isEnabled = true
                    when (exception) {
                        is FirebaseAuthUserCollisionException -> {
                            tilEmail.error = "Este e-mail já está cadastrado"
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            tilPassword.error = "Senha muito fraca"
                        }
                        else -> {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Erro ao realizar cadastro: ${exception.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
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