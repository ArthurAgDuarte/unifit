package com.example.unifit

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MapaAcademiaActivity : AppCompatActivity() {

    private lateinit var scrollContent: ScrollView
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSearchSubmit: MaterialButton
    private lateinit var btnNotification: ImageButton
    private lateinit var cardMap: MaterialCardView
    private lateinit var tvSelectedLocationOnMap: TextView

    private lateinit var cardLoc1: MaterialCardView
    private lateinit var cardLoc2: MaterialCardView
    private lateinit var cardLoc3: MaterialCardView
    private lateinit var cardLoc4: MaterialCardView
    private lateinit var cardLoc5: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa_academia)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        handleIntentData()
        setupListeners()
        configurarMenuDesenhado(this, R.id.nav_mapa)
    }

    private fun initViews() {
        scrollContent = findViewById(R.id.scrollContent)
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        btnSearchSubmit = findViewById(R.id.btnSearchSubmit)
        btnNotification = findViewById(R.id.btnNotification)
        cardMap = findViewById(R.id.cardMap)
        tvSelectedLocationOnMap = findViewById(R.id.tvSelectedLocationOnMap)

        cardLoc1 = findViewById(R.id.cardLoc1)
        cardLoc2 = findViewById(R.id.cardLoc2)
        cardLoc3 = findViewById(R.id.cardLoc3)
        cardLoc4 = findViewById(R.id.cardLoc4)
        cardLoc5 = findViewById(R.id.cardLoc5)
    }

    private fun handleIntentData() {
        val extraData = intent.getStringExtra("USER_EMAIL")
            ?: intent.getStringExtra("SEARCH_QUERY")
            ?: intent.getStringExtra("email")
            ?: intent.getStringExtra("query")

        if (!extraData.isNullOrBlank()) {
            etSearch.setText(extraData)
            processSearchOrLocation(extraData)
        }
    }

    private fun setupListeners() {
        btnSearchSubmit.setOnClickListener {
            val query = etSearch.text?.toString()?.trim().orEmpty()
            if (query.isEmpty()) {
                tilSearch.error = getString(R.string.error_empty_search)
            } else {
                tilSearch.error = null
                processSearchOrLocation(query)
            }
        }

        btnNotification.setOnClickListener {
            abrirTela(this, NotificacoesActivity::class.java) // RF06.4
        }

        cardLoc1.setOnClickListener {
            abrirTela(this, DetalheEspacoActivity::class.java) // RF11.4
        }

        cardLoc2.setOnClickListener {
            abrirTela(this, DetalheEspacoActivity::class.java) // RF11.4
        }

        cardLoc3.setOnClickListener {
            abrirTela(this, DetalheEspacoActivity::class.java) // RF11.4
        }

        cardLoc4.setOnClickListener {
            abrirTela(this, DetalheEspacoActivity::class.java) // RF11.4
        }

        cardLoc5.setOnClickListener {
            abrirTela(this, DetalheEspacoActivity::class.java) // RF11.4
        }
    }

    private fun processSearchOrLocation(query: String) {
        val matchedLocation = when {
            query.contains("1") || query.contains("musculacao", ignoreCase = true) -> getString(R.string.loc_1_title)
            query.contains("2") || query.contains("cardio", ignoreCase = true) -> getString(R.string.loc_2_title)
            query.contains("3") || query.contains("spinning", ignoreCase = true) -> getString(R.string.loc_3_title)
            query.contains("4") || query.contains("funcional", ignoreCase = true) -> getString(R.string.loc_4_title)
            query.contains("5") || query.contains("vestuario", ignoreCase = true) -> getString(R.string.loc_5_title)
            else -> query
        }

        selectAndScrollToMap(matchedLocation)
    }

    private fun selectAndScrollToMap(locationName: String) {
        tvSelectedLocationOnMap.text = getString(R.string.map_showing_location, locationName)

        scrollContent.post {
            scrollContent.smoothScrollTo(0, cardMap.top)
        }

        Toast.makeText(this, getString(R.string.toast_location_selected, locationName), Toast.LENGTH_SHORT).show()
    }
}