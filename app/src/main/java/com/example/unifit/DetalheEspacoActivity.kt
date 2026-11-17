package com.example.unifit

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DetalheEspacoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_espaco)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSeeExercises = findViewById<MaterialButton>(R.id.btnSeeExercises)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSeeExercises.setOnClickListener {
            showExercisesBottomSheet()
        }
    }

    private fun showExercisesBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dpToPx(), 12.dpToPx(), 20.dpToPx(), 24.dpToPx())
            setBackgroundColor(getColor(R.color.white))
        }

        val handle = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 4.dpToPx()).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = 16.dpToPx()
            }
            background = ContextCompat.getDrawable(this@DetalheEspacoActivity, R.drawable.bg_circle_button)
        }
        rootLayout.addView(handle)

        val tvTitle = TextView(this).apply {
            text = getString(R.string.sheet_title)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.text_primary))
        }
        rootLayout.addView(tvTitle)

        val tvSubtitle = TextView(this).apply {
            text = getString(R.string.sheet_subtitle)
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, 4.dpToPx(), 0, 16.dpToPx())
        }
        rootLayout.addView(tvSubtitle)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val cardsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        cardsContainer.addView(createExerciseCard(
            getString(R.string.ex1_title),
            getString(R.string.ex1_sets),
            getString(R.string.ex1_desc)
        ))

        cardsContainer.addView(createExerciseCard(
            getString(R.string.ex2_title),
            getString(R.string.ex2_sets),
            getString(R.string.ex2_desc)
        ))

        cardsContainer.addView(createExerciseCard(
            getString(R.string.ex3_title),
            getString(R.string.ex3_sets),
            getString(R.string.ex3_desc)
        ))

        scrollView.addView(cardsContainer)
        rootLayout.addView(scrollView)

        val btnClose = MaterialButton(this).apply {
            text = getString(R.string.btn_close_sheet)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.white))
            setBackgroundColor(getColor(R.color.primary_blue))
            cornerRadius = 12.dpToPx()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                52.dpToPx()
            ).apply {
                topMargin = 16.dpToPx()
            }
            setOnClickListener {
                bottomSheetDialog.dismiss()
            }
        }
        rootLayout.addView(btnClose)

        bottomSheetDialog.setContentView(rootLayout)
        bottomSheetDialog.show()
    }

    private fun createExerciseCard(title: String, sets: String, desc: String): MaterialCardView {
        val card = MaterialCardView(this).apply {
            radius = 12.dpToPx().toFloat()
            cardElevation = 0f
            strokeWidth = 1.dpToPx()
            setStrokeColor(getColorStateList(R.color.card_stroke))
            setCardBackgroundColor(getColor(R.color.bg_main))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12.dpToPx()
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14.dpToPx(), 14.dpToPx(), 14.dpToPx(), 14.dpToPx())
        }

        val tvCardTitle = TextView(this).apply {
            text = title
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.text_primary))
        }

        val tvCardSets = TextView(this).apply {
            text = sets
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.primary_blue))
            setPadding(0, 2.dpToPx(), 0, 4.dpToPx())
        }

        val tvCardDesc = TextView(this).apply {
            text = desc
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
        }

        container.addView(tvCardTitle)
        container.addView(tvCardSets)
        container.addView(tvCardDesc)

        card.addView(container)
        return card
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
