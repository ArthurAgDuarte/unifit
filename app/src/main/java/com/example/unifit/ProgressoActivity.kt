package com.example.unifit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
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

class WeightGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val primaryBlueColor = ContextCompat.getColor(context, R.color.primary_blue_progresso)
    private val strokeColor = ContextCompat.getColor(context, R.color.card_stroke)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryBlueColor
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val dotOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryBlueColor
        style = Paint.Style.FILL
    }

    private val dotInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val path = Path()
    private var dataPoints = floatArrayOf(0.20f, 0.28f, 0.55f, 0.82f)

    fun updatePoints(points: FloatArray) {
        dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if ((w == 0f) || (h == 0f)) return

        canvas.drawLine(0f, h * 0.35f, w, h * 0.35f, gridPaint)
        canvas.drawLine(0f, h * 0.70f, w, h * 0.70f, gridPaint)

        val x1 = w * 0.05f
        val y1 = h * dataPoints.getOrElse(0) { 0.20f }

        val x2 = w * 0.35f
        val y2 = h * dataPoints.getOrElse(1) { 0.28f }

        val x3 = w * 0.65f
        val y3 = h * dataPoints.getOrElse(2) { 0.55f }

        val x4 = w * 0.95f
        val y4 = h * dataPoints.getOrElse(3) { 0.82f }

        path.reset()
        path.moveTo(x1, y1)

        path.cubicTo((x1 + x2) / 2, y1, (x1 + x2) / 2, y2, x2, y2)
        path.cubicTo((x2 + x3) / 2, y2, (x2 + x3) / 2, y3, x3, y3)
        path.cubicTo((x3 + x4) / 2, y3, (x3 + x4) / 2, y4, x4, y4)

        canvas.drawPath(path, linePaint)

        canvas.drawCircle(x4, y4, 12f, dotOuterPaint)
        canvas.drawCircle(x4, y4, 6f, dotInnerPaint)
    }
}

class ProgressoActivity : AppCompatActivity() {

    private lateinit var btnFilterSemana: TextView
    private lateinit var btnFilterMes: TextView
    private lateinit var btnFilterTresMeses: TextView
    private lateinit var tvVerTodas: TextView
    private lateinit var tvPesoValor: TextView
    private lateinit var tvPesoVariacao: TextView
    private lateinit var tvFrequenciaDias: TextView
    private lateinit var weightGraphView: WeightGraphView

    private lateinit var cardConquista1: LinearLayout
    private lateinit var cardConquista2: LinearLayout
    private lateinit var cardConquista3: LinearLayout

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPeso: TextInputLayout
    private lateinit var etPeso: TextInputEditText
    private lateinit var btnSalvarProgresso: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progresso)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        handleIntentExtra()
    }

    private fun initViews() {
        btnFilterSemana = findViewById(R.id.btnFilterSemana)
        btnFilterMes = findViewById(R.id.btnFilterMes)
        btnFilterTresMeses = findViewById(R.id.btnFilterTresMeses)
        tvVerTodas = findViewById(R.id.tvVerTodas)
        tvPesoValor = findViewById(R.id.tvPesoValor)
        tvPesoVariacao = findViewById(R.id.tvPesoVariacao)
        tvFrequenciaDias = findViewById(R.id.tvFrequenciaDias)
        weightGraphView = findViewById(R.id.weightGraphView)

        cardConquista1 = findViewById(R.id.cardConquista1)
        cardConquista2 = findViewById(R.id.cardConquista2)
        cardConquista3 = findViewById(R.id.cardConquista3)

        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPeso = findViewById(R.id.tilPeso)
        etPeso = findViewById(R.id.etPeso)
        btnSalvarProgresso = findViewById(R.id.btnSalvarProgresso)
    }

    private fun setupListeners() {
        findViewById<android.view.View>(R.id.btnVoltar).setOnClickListener { finish() }

        btnFilterSemana.setOnClickListener {
            selectFilter(
                btnFilterSemana,
                getString(R.string.filter_semana),
                getString(R.string.variacao_semana),
                getString(R.string.frequencia_semana),
                floatArrayOf(0.40f, 0.45f, 0.35f, 0.50f)
            )
        }

        btnFilterMes.setOnClickListener {
            selectFilter(
                btnFilterMes,
                getString(R.string.filter_mes),
                getString(R.string.variacao_mes),
                getString(R.string.frequencia_mes),
                floatArrayOf(0.20f, 0.28f, 0.55f, 0.82f)
            )
        }

        btnFilterTresMeses.setOnClickListener {
            selectFilter(
                btnFilterTresMeses,
                getString(R.string.filter_tres_meses),
                getString(R.string.variacao_tres_meses),
                getString(R.string.frequencia_tres_meses),
                floatArrayOf(0.15f, 0.35f, 0.60f, 0.90f)
            )
        }

        tvVerTodas.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_ver_todas), Toast.LENGTH_SHORT).show()
        }

        cardConquista1.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_conquista_1), Toast.LENGTH_SHORT).show()
        }

        cardConquista2.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_conquista_2), Toast.LENGTH_SHORT).show()
        }

        cardConquista3.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_conquista_3), Toast.LENGTH_SHORT).show()
        }

        btnSalvarProgresso.setOnClickListener {
            hideKeyboard()
            validarESalvar()
        }
    }

    private fun selectFilter(
        selectedView: TextView,
        nomeFiltro: String,
        variacaoText: String,
        frequenciaText: String,
        graphPoints: FloatArray
    ) {
        val pills = listOf(btnFilterSemana, btnFilterMes, btnFilterTresMeses)
        pills.forEach { pill ->
            if (pill == selectedView) {
                pill.setBackgroundResource(R.drawable.bg_pill_selected)
                pill.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                pill.setBackgroundResource(R.drawable.bg_pill_unselected)
                pill.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
        }

        tvPesoVariacao.text = variacaoText
        tvFrequenciaDias.text = frequenciaText
        weightGraphView.updatePoints(graphPoints)

        Toast.makeText(this, getString(R.string.toast_filtro_selecionado, nomeFiltro), Toast.LENGTH_SHORT).show()
    }

    private fun handleIntentExtra() {
        val emailExtra = intent.getStringExtra("USER_EMAIL")
            ?: intent.getStringExtra("email")
            ?: intent.getStringExtra("EMAIL")

        if (!emailExtra.isNullOrEmpty()) {
            etEmail.setText(emailExtra)
        }
    }

    private fun validarESalvar() {
        tilEmail.error = null
        tilPeso.error = null

        val email = etEmail.text.toString().trim()
        val pesoRaw = etPeso.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.erro_email_vazio)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.erro_email_invalido)
            isValid = false
        }

        if (pesoRaw.isEmpty()) {
            tilPeso.error = getString(R.string.erro_peso_vazio)
            isValid = false
        }

        if (isValid) {
            val pesoFormatado = pesoRaw.replace('.', ',')
            val valorPesoTexto = "$pesoFormatado kg"
            tvPesoValor.text = valorPesoTexto

            weightGraphView.updatePoints(floatArrayOf(0.20f, 0.28f, 0.55f, 0.92f))

            Toast.makeText(this, getString(R.string.sucesso_salvar), Toast.LENGTH_SHORT).show()
            etPeso.text?.clear()
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}