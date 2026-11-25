package com.example.unifit
import com.example.unifit.ui.notificacoes.NotificacoesActivity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class GymBotActivity : TelaBase() {

    private lateinit var inputLayoutMsg: TextInputLayout
    private lateinit var edtMsg: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var btnReserveClass: MaterialButton
    private lateinit var scrollViewChat: ScrollView
    private lateinit var dynamicMessagesContainer: LinearLayout

    private lateinit var suggestionButtons: List<MaterialButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gym_bot)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        handleIntentExtra()
        setupListeners()
    }

    private fun initViews() {
        inputLayoutMsg = findViewById(R.id.inputLayoutMsg)
        edtMsg = findViewById(R.id.edtMsg)
        btnSend = findViewById(R.id.btnSend)
        btnReserveClass = findViewById(R.id.btnReserveClass)
        scrollViewChat = findViewById(R.id.scrollViewChat)
        dynamicMessagesContainer = findViewById(R.id.dynamicMessagesContainer)

        suggestionButtons = listOf(
            findViewById(R.id.btnSuggestion1),
            findViewById(R.id.btnSuggestion2),
            findViewById(R.id.btnSuggestion3),
            findViewById(R.id.btnSuggestion4),
            findViewById(R.id.btnSuggestion5)
        )
    }

    private fun handleIntentExtra() {
        val extraText = intent.getStringExtra("USER_EMAIL")
            ?: intent.getStringExtra("EXTRA_EMAIL")
            ?: intent.getStringExtra("EXTRA_MESSAGE")
            ?: intent.getStringExtra("EXTRA_QUERY")

        if (!extraText.isNullOrEmpty()) {
            edtMsg.setText(extraText)
        }
    }

    private fun setupListeners() {
        findViewById<android.view.View>(R.id.btnVoltar).setOnClickListener { finish() }

        btnSend.setOnClickListener {
            val text = edtMsg.text?.toString()?.trim().orEmpty()
            if (validateInput(text)) {
                sendUserMessage(text)
                edtMsg.text?.clear()
            }
        }

        findViewById<android.view.View>(R.id.btnNotification).setOnClickListener {
            abrirTela(this, NotificacoesActivity::class.java) // RF06.4
        }

        // Card da aula de Spinning -> Aulas (RF18.3)
        findViewById<android.view.View>(R.id.cardClassItem).setOnClickListener { abrirAulaDeSpinning() }

        btnReserveClass.setOnClickListener { abrirAulaDeSpinning() } // RF18.3

        suggestionButtons.forEach { button ->
            button.setOnClickListener {
                val questionText = button.text.toString()
                sendUserMessage(questionText)
            }
        }

        edtMsg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputLayoutMsg.error = null
                inputLayoutMsg.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Abre o Detalhe da Aula de Spinning de hoje (onde a reserva é confirmada)
    private fun abrirAulaDeSpinning() {
        val indice = listaAulas.indexOfFirst { it.categoria == "Spinning" && it.diaOffset == 0 }.coerceAtLeast(0)
        val intent = android.content.Intent(this, DetalheAulaActivity::class.java)
        intent.putExtra("indiceAula", indice)
        startActivity(intent)
    }

    private fun validateInput(input: String): Boolean {
        if (input.isEmpty()) {
            inputLayoutMsg.isErrorEnabled = true
            inputLayoutMsg.error = getString(R.string.error_empty_input)
            return false
        }

        if (input.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            inputLayoutMsg.isErrorEnabled = true
            inputLayoutMsg.error = getString(R.string.error_invalid_email_gymbot)
            return false
        }

        inputLayoutMsg.error = null
        inputLayoutMsg.isErrorEnabled = false
        return true
    }

    private fun sendUserMessage(userMessageText: String) {
        addUserBubble(userMessageText)

        val botResponseText = getBotResponse(userMessageText)
        addBotBubble(botResponseText)

        scrollToBottom()
    }

    private fun getBotResponse(message: String): String {
        val lowerMsg = message.lowercase()
        return when {
            lowerMsg.contains("treino") -> getString(R.string.response_treino)
            lowerMsg.contains("reservar") || lowerMsg.contains("aula") -> getString(R.string.response_reservar)
            lowerMsg.contains("spinning") || lowerMsg.contains("horário") || lowerMsg.contains("horario") -> getString(R.string.response_spinning)
            lowerMsg.contains("plano") -> getString(R.string.response_plano)
            lowerMsg.contains("personal") -> getString(R.string.response_personal)
            else -> getString(R.string.response_default)
        }
    }

    private fun addUserBubble(message: String) {
        val userTextView = TextView(this).apply {
            text = message
            setTextColor(ContextCompat.getColor(context, R.color.white))
            textSize = 15f
            setBackgroundResource(R.drawable.bg_user_bubble)
            setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dpToPx(12)
        }

        dynamicMessagesContainer.addView(userTextView, params)
    }

    private fun addBotBubble(message: String) {
        val botTextView = TextView(this).apply {
            text = message
            setTextColor(ContextCompat.getColor(context, R.color.text_primary_gymbot))
            textSize = 15f
            setLineSpacing(dpToPx(3).toFloat(), 1.0f)
            setBackgroundResource(R.drawable.bg_bot_bubble)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dpToPx(12)
        }

        dynamicMessagesContainer.addView(botTextView, params)
    }

    private fun scrollToBottom() {
        scrollViewChat.post {
            scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}