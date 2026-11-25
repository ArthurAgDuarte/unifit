package com.example.unifit
import com.example.unifit.ui.notificacoes.NotificacoesActivity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReservasActivity : TelaBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reservas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navegação: sino -> Notificações (RF06.4) e menu inferior (RF06)
        findViewById<android.view.View>(R.id.btnNotificacoes).setOnClickListener {
            abrirTela(this, NotificacoesActivity::class.java)
        }
        configurarMenuDesenhado(this, R.id.nav_aulas)
    }
}