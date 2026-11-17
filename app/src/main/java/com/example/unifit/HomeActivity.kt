package com.example.unifit
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Navegação (RF06, RF07)
        findViewById<View>(R.id.btnNotificacoes).setOnClickListener {
            abrirTela(this, NotificacoesActivity::class.java)
        }
        findViewById<View>(R.id.btnContinuarTreino).setOnClickListener {
            abrirTela(this, DetalheTreinoActivity::class.java) // abre o Treino A (índice 0)
        }
        findViewById<View>(R.id.atalhoMapa).setOnClickListener { abrirAba(this, R.id.nav_mapa) }
        findViewById<View>(R.id.atalhoAulas).setOnClickListener { abrirAba(this, R.id.nav_aulas) }
        findViewById<View>(R.id.btnVerTodasReservas).setOnClickListener {
            abrirTela(this, ReservasActivity::class.java)
        }
        // Próxima aula reservada -> Detalhe da Aula (RF07.5)
        findViewById<View>(R.id.cardProximaAula).setOnClickListener {
            val indice = listaAulas.indexOfFirst { it.reservada }
            if (indice >= 0) {
                val intent = android.content.Intent(this, DetalheAulaActivity::class.java)
                intent.putExtra("indiceAula", indice)
                startActivity(intent)
            }
        }
        configurarMenuDesenhado(this, R.id.nav_inicio)
    }

    override fun onResume() {
        super.onResume()
        // Progresso do treino do dia (RF07.1 e RF07.2), atualizado pelo "Concluir exercício" (RF10.3)
        if (listaFichas.isEmpty()) return
        val ficha = listaFichas[0]
        val total = ficha.exercicios.size
        val feitos = ficha.concluidos.size
        val percentual = if (total > 0) feitos * 100 / total else 0
        findViewById<android.widget.TextView>(R.id.txtProgressoTreino).text = "$feitos de $total concluídos"
        findViewById<android.widget.TextView>(R.id.txtPercentualTreino).text = "$percentual%"
        findViewById<android.widget.ProgressBar>(R.id.progressoTreinoDia).progress = percentual
    }
}
