package com.example.unifit

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DashboardAdmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_adm)

        setupWindowInsets()
        setupIntentExtras()
        setupFormValidation()
        setupClickListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupIntentExtras() {
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)

        intent?.let { extra ->
            extra.getStringExtra("USER_NAME")?.let { name ->
                if (name.isNotBlank()) {
                    tvGreeting.text = getString(R.string.greeting_format, name)
                }
            }
            extra.getStringExtra("USER_EMAIL")?.let { email ->
                if (email.isNotBlank()) {
                    etEmail.setText(email)
                }
            }
        }
    }

    private fun setupFormValidation() {
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        etEmail.doOnTextChanged { _, _, _, _ ->
            tilEmail.error = null
        }

        btnSearch.setOnClickListener {
            val email = etEmail.text.toString().trim()

            when {
                email.isEmpty() -> {
                    tilEmail.error = getString(R.string.error_empty_email)
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    tilEmail.error = getString(R.string.error_invalid_email_adm)
                }
                else -> {
                    tilEmail.error = null
                    showDialog(
                        title = getString(R.string.dialog_user_found_title),
                        message = getString(R.string.dialog_user_found_msg, email)
                    )
                }
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.ivNotification).setOnClickListener {
            showDialog(
                title = getString(R.string.dialog_notifications_title),
                message = getString(R.string.dialog_notifications_msg)
            )
        }

        val logoClick = View.OnClickListener {
            Toast.makeText(this, "Painel Admin • Sistema Operacional", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.vHeaderLogoBg).setOnClickListener(logoClick)
        findViewById<TextView>(R.id.tvHeaderTitle).setOnClickListener(logoClick)

        findViewById<MaterialCardView>(R.id.cvStat1).setOnClickListener {
            showActionDialog(
                title = getString(R.string.dialog_stat1_title),
                message = getString(R.string.dialog_stat1_msg),
                positiveBtnText = getString(R.string.btn_action_add_user),
                positiveAction = {
                    Toast.makeText(this, "Acessando formulário de novo aluno...", Toast.LENGTH_SHORT).show()
                }
            )
        }

        findViewById<MaterialCardView>(R.id.cvStat2).setOnClickListener {
            showActionDialog(
                title = getString(R.string.dialog_stat2_title),
                message = getString(R.string.dialog_stat2_msg),
                positiveBtnText = getString(R.string.btn_action_view_schedule),
                positiveAction = {
                    Toast.makeText(this, "Abrindo grade horária de aulas...", Toast.LENGTH_SHORT).show()
                }
            )
        }

        findViewById<MaterialCardView>(R.id.cvStat3).setOnClickListener {
            showActionDialog(
                title = getString(R.string.dialog_stat3_title),
                message = getString(R.string.dialog_stat3_msg),
                positiveBtnText = getString(R.string.btn_action_report_issue),
                positiveAction = {
                    Toast.makeText(this, "Abrindo chamado de manutenção...", Toast.LENGTH_SHORT).show()
                }
            )
        }

        findViewById<MaterialCardView>(R.id.cvOccupation).setOnClickListener {
            showDialog(
                title = getString(R.string.dialog_occupation_title),
                message = getString(R.string.dialog_occupation_msg)
            )
        }

        findViewById<MaterialCardView>(R.id.cvQuick1).setOnClickListener {
            startActivity(android.content.Intent(this, GerenciarUsuariosActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cvQuick2).setOnClickListener {
            startActivity(android.content.Intent(this, GerenciarAulasActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cvQuick3).setOnClickListener {
            startActivity(android.content.Intent(this, ExerciciosAparelhosActivity::class.java))
        }
    }

    private fun showDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.btn_close, null)
            .show()
    }

    private fun showActionDialog(
        title: String,
        message: String,
        positiveBtnText: String,
        positiveAction: () -> Unit,
        negativeBtnText: String? = null,
        negativeAction: (() -> Unit)? = null
    ) {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveBtnText) { _, _ -> positiveAction() }

        if (negativeBtnText != null && negativeAction != null) {
            builder.setNegativeButton(negativeBtnText) { _, _ -> negativeAction() }
        } else {
            builder.setNeutralButton(R.string.btn_close, null)
        }

        builder.show()
    }
}