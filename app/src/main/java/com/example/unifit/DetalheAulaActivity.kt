package com.example.unifit

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Tela "Detalhe da Aula" (RF13, pág. 32). Recebe o extra "indiceAula"
class DetalheAulaActivity : TelaBase() {
    lateinit var btnVoltar: View
    lateinit var imgIconeAula: ImageView
    lateinit var txtNomeAula: TextView
    lateinit var txtStatusAula: TextView
    lateinit var txtCategoriaAula: TextView
    lateinit var txtHorarioAula: TextView
    lateinit var txtLocalAula: TextView
    lateinit var txtProfessorAula: TextView
    lateinit var txtVagasAula: TextView
    lateinit var txtDescricaoAula: TextView
    lateinit var btnReservarVaga: MaterialButton

    lateinit var aula: Aula

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_aula)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        imgIconeAula = findViewById(R.id.imgIconeAula)
        txtNomeAula = findViewById(R.id.txtNomeAula)
        txtStatusAula = findViewById(R.id.txtStatusAula)
        txtCategoriaAula = findViewById(R.id.txtCategoriaAula)
        txtHorarioAula = findViewById(R.id.txtHorarioAula)
        txtLocalAula = findViewById(R.id.txtLocalAula)
        txtProfessorAula = findViewById(R.id.txtProfessorAula)
        txtVagasAula = findViewById(R.id.txtVagasAula)
        txtDescricaoAula = findViewById(R.id.txtDescricaoAula)
        btnReservarVaga = findViewById(R.id.btnReservarVaga)

        aula = listaAulas[intent.getIntExtra("indiceAula", 0)]

        btnVoltar.setOnClickListener { finish() }
        btnReservarVaga.setOnClickListener {
            if (aula.reservada) confirmarCancelamento() else mostrarConfirmacao()
        }

        mostrarAula()
    }

    fun mostrarAula() {
        imgIconeAula.setImageResource(aula.icone)
        txtNomeAula.text = aula.nome
        txtCategoriaAula.text = "Aula coletiva • ${aula.categoria}"
        txtHorarioAula.text = "${nomeDoDia(aula.diaOffset)}, ${aula.horario}"
        txtLocalAula.text = aula.local
        txtProfessorAula.text = aula.professor
        txtVagasAula.text = "${aula.vagasRestantes().coerceAtLeast(0)} de ${aula.vagas} disponíveis"
        txtDescricaoAula.text = descricaoDaCategoria(aula.categoria)

        // Etiqueta e botão mudam conforme a situação da aula
        when {
            aula.reservada -> {
                txtStatusAula.text = "Reservada"
                txtStatusAula.setBackgroundResource(R.drawable.bg_status_ativo)
                btnReservarVaga.text = "Cancelar reserva"
                btnReservarVaga.isEnabled = true
            }
            aula.lotada() -> {
                txtStatusAula.text = "Lotada"
                txtStatusAula.setBackgroundResource(R.drawable.bg_status_inativo)
                btnReservarVaga.text = "Aula lotada"
                btnReservarVaga.isEnabled = false
            }
            else -> {
                txtStatusAula.text = "Vagas abertas"
                txtStatusAula.setBackgroundResource(R.drawable.bg_status_ativo)
                btnReservarVaga.text = "Reservar vaga"
                btnReservarVaga.isEnabled = true
            }
        }
    }

    // Pop-up "Confirmar reserva?" (RF13.1)
    fun mostrarConfirmacao() {
        val view = layoutInflater.inflate(R.layout.dialog_confirmar_reserva, null)
        view.findViewById<TextView>(R.id.txtResumoReserva).text =
            "${aula.nome} • ${nomeDoDia(aula.diaOffset)}, ${aula.horario}\n${aula.local} • ${aula.professor}"

        val dialogo = MaterialAlertDialogBuilder(this).setView(view).show()

        view.findViewById<MaterialButton>(R.id.btnConfirmarReserva).setOnClickListener {
            // Registra a reserva e atualiza as vagas (RF13.2)
            aula.reservada = true
            aula.inscritos++
            Toast.makeText(this, "Reserva confirmada!", Toast.LENGTH_SHORT).show()
            dialogo.dismiss()
            mostrarAula()
        }
        view.findViewById<MaterialButton>(R.id.btnCancelarReserva).setOnClickListener { dialogo.dismiss() }
    }

    fun confirmarCancelamento() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cancelar reserva?")
            .setMessage("Sua vaga em ${aula.nome} (${nomeDoDia(aula.diaOffset)}, ${aula.horario}) será liberada.")
            .setNegativeButton("Manter", null)
            .setPositiveButton("Cancelar reserva") { _, _ ->
                aula.reservada = false
                aula.inscritos--
                mostrarAula()
            }
            .show()
    }
}
