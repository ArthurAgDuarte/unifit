package com.example.unifit

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

// Configura o menu inferior igual em todas as abas.
// Cada aba é uma Activity: ao trocar, abre a nova (sem animação) e fecha a atual.
// telaPrincipal = false para telas "dentro" de uma aba (ex.: Notificações dentro do Início):
// nelas, tocar na aba que já está marcada abre a tela principal dela.
fun configurarMenuInferior(
    activity: AppCompatActivity,
    bottomNavigation: BottomNavigationView,
    abaAtual: Int,
    telaPrincipal: Boolean = true
) {
    bottomNavigation.selectedItemId = abaAtual

    // Tocou em outra aba
    bottomNavigation.setOnItemSelectedListener { item ->
        abrirAba(activity, item.itemId)
        true
    }

    // Tocou na aba que já está marcada
    bottomNavigation.setOnItemReselectedListener { item ->
        if (!telaPrincipal) abrirAba(activity, item.itemId)
    }
}

// Para as telas com o menu inferior desenhado no XML (LinearLayouts em vez de
// BottomNavigationView): cada item precisa ter o id da aba (nav_inicio, nav_treinos...).
fun configurarMenuDesenhado(activity: AppCompatActivity, abaAtual: Int) {
    val abas = listOf(R.id.nav_inicio, R.id.nav_treinos, R.id.nav_aulas, R.id.nav_mapa, R.id.nav_perfil)
    for (aba in abas) {
        activity.findViewById<View>(aba)?.setOnClickListener {
            if (aba != abaAtual) abrirAba(activity, aba)
        }
    }
}

fun abrirAba(activity: AppCompatActivity, itemId: Int) {
    val destino = when (itemId) {
        R.id.nav_inicio -> HomeActivity::class.java // Início da Celina
        R.id.nav_treinos -> com.example.unifit.ui.main.MainActivity::class.java // abre na aba Meus Treinos
        R.id.nav_aulas -> AulasActivity::class.java
        R.id.nav_mapa -> MapaAcademiaActivity::class.java // Mapa da Academia (Mapa acad)
        else -> PerfilActivity::class.java
    }
    val intent = Intent(activity, destino)
    // CLEAR_TOP: se a aba já estiver aberta por baixo (ex.: Início -> Notificações -> Início), volta para ela
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    activity.startActivity(intent)
    activity.finish()
}

// Abre uma tela "por cima" da atual (o botão voltar retorna)
fun abrirTela(activity: AppCompatActivity, destino: Class<*>) {
    activity.startActivity(Intent(activity, destino))
}

// Abre uma tela e apaga o histórico (entrar, sair da conta): voltar não retorna.
// Usa finishAffinity() em vez de NEW_TASK | CLEAR_TASK: essas flags fazem o Android
// mostrar a splash do sistema na troca, e essa animação pode travar com a tela preta.
fun abrirTelaLimpandoHistorico(activity: AppCompatActivity, destino: Class<*>) {
    activity.startActivity(Intent(activity, destino))
    activity.finishAffinity()
}
