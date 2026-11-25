# UniFit — Resumo da integração com o Firebase

Este documento tem duas partes:

- **Parte 1:** o que foi feito no projeto, por quê, e como substituir cada peça.
- **Parte 2:** cada componente do Android usado (Fragment, RecyclerView, layouts, etc.), com motivação e como trocar.

**Escopo da integração:** Splash, Onboarding, Meus Treinos, Detalhe do Treino, Notificações e Acessibilidade, usando Firestore e login anônimo. As outras telas (dos colegas, no pacote raiz) receberam só os ajustes necessários para compilar.

**Bibliotecas que não podiam ser usadas:** Compose, Navigation, Hilt, Coroutines, Room, Retrofit, ViewModel e Glide.

---

# Parte 1 — O que foi feito

## 1. Gradle e dependências

**O que mudou**
- [gradle/libs.versions.toml](gradle/libs.versions.toml): entraram Firebase BoM `35.2.0`, `firebase-firestore`, `firebase-auth`, o plugin `google-services 4.6.0`, RecyclerView, ViewPager2 e Fragment KTX.
- [build.gradle.kts](build.gradle.kts) (raiz): declara o plugin `org.jetbrains.kotlin.android 2.4.21` com `apply false`.
- [app/build.gradle.kts](app/build.gradle.kts): aplica o `google-services` e liga `buildConfig = true`.

**Por quê**
- O BoM controla as versões do Firebase, então as bibliotecas não têm versão própria.
- O Firebase BoM 35 foi compilado com Kotlin 2.4, e o Kotlin embutido no AGP 9.3 é mais antigo. Declarar o plugin Kotlin na raiz, mesmo sem aplicá-lo, obriga o build a usar o compilador 2.4.
- O `buildConfig` gera o `BuildConfig.DEBUG`, que garante que o seed nunca rode em release.

**Como substituir**
- Para mudar a versão do Firebase, troque só `firebaseBom`. Se a versão nova pedir outro Kotlin, ajuste `kotlin` junto.
- Se um dia o AGP trouxer Kotlin 2.4 ou mais novo, dá para tirar a linha `kotlin-android` da raiz.
- Para usar outro projeto Firebase, troque o `app/google-services.json`. Ele define em qual projeto o app grava.

---

## 2. Organização do código

As 6 telas do escopo saíram do pacote raiz e foram para pacotes próprios:

| Pacote | Conteúdo |
|---|---|
| `model/` | `Ficha`, `ItemExercicio`, `Progresso`, `Notificacao`, `Preferencias` (data classes com valor padrão, que o `toObject()` do Firestore exige) |
| `data/` | `FirebaseHelper`, os repositórios e o `SeedDebug` |
| `ui/<tela>/` | as Activities, Fragments e Adapters de cada tela |
| `util/` | `Constantes`, `Datas`, `Formatos`, `Fotos`, `Mensagens`, `PreferenciasLocais`, `Tema` |

**Por quê:** separar as telas (ui), o acesso ao banco (data) e os dados (model). As telas nunca falam com o Firestore direto, só com os repositórios.

**Como substituir:** para trocar o Firestore por outro back-end, basta reescrever as classes de `data/` mantendo as mesmas assinaturas (`onSuccess`/`onError`). As telas continuam funcionando.

---

## 3. Acesso ao Firebase

### Login anônimo e usuário de demonstração
- [FirebaseHelper.kt](app/src/main/java/com/example/unifit/data/FirebaseHelper.kt) faz `signInAnonymously()` e reaproveita a sessão se ela já existir.
- Todos os dados ficam em `users/ana` (`Constantes.USUARIO_DEMO_ID`).

**Por quê:** o login ainda não existe. A autenticação anônima serve só para as regras de segurança aceitarem as leituras e escritas.

**Como substituir, quando o login real existir:**
1. Em [Constantes.kt](app/src/main/java/com/example/unifit/util/Constantes.kt) e no `FirebaseHelper.usuarioDemo`, troque `"ana"` por `FirebaseAuth.getInstance().currentUser!!.uid`.
2. No `FichaRepository`, o filtro `whereEqualTo("alunoId", ...)` passa a usar o uid.
3. Troque as regras (veja a seção 5).
4. Na Splash, troque `entrarAnonimo` por uma checagem de "já está logado?" que, se não estiver, manda para a `LoginActivity`.

### Estrutura no Firestore

| Caminho | Repositório | Uso |
|---|---|---|
| `fichas/{id}` | `FichaRepository` | fichas do dia (`whereArrayContains("diasSemana", 1..7)`) e o detalhe |
| `users/ana/progresso/{yyyy-MM-dd}` | `ProgressoRepository` | "Iniciar treino" (`set` com merge e `serverTimestamp`) |
| `users/ana/notificacoes/{id}` | `NotificacaoRepository` | lista e badge em tempo real (`addSnapshotListener`), "marcar todas" com `WriteBatch` |
| `users/ana/preferencias/acessibilidade` | `PreferenciasRepository` | as opções de acessibilidade |

Algumas decisões que valem saber:
- **A ordenação das fichas é feita no app**, não com `orderBy`. Assim não é preciso criar um índice composto no console. Se as fichas crescerem muito, crie o índice e mova a ordenação para a consulta.
- **As preferências ficam em `.../preferencias/acessibilidade`** porque o Firestore exige um id de documento. O caminho original pedido era de coleção.
- **Os exercícios são uma lista dentro da ficha**, não uma subcoleção. Assim uma leitura só traz tudo.

---

## 4. Seed de dados (só em debug)

[SeedDebug.kt](app/src/main/java/com/example/unifit/data/SeedDebug.kt) grava os dados do protótipo num único `WriteBatch`: `users/ana`, as fichas `treino_a`, `treino_b` e `treino_c`, as notificações e as preferências. A Splash roda o seed se `fichas/treino_a` não existir.

**Por quê:** ter dados para testar sem digitar nada no console. Os ids são fixos, então rodar de novo sobrescreve em vez de duplicar.

**Como substituir ou remover:**
- Para mudar os dados de teste, edite as funções do `SeedDebug`, apague `fichas/treino_a` no console e abra o app.
- Para remover o seed, apague o arquivo e, na [SplashActivity.kt](app/src/main/java/com/example/unifit/ui/splash/SplashActivity.kt), troque `popularBancoSeDebug()` por `carregarPreferencias()`.

---

## 5. Regras de segurança

[firestore.rules](firestore.rules) funciona assim:
- `fichas`: leitura para quem está autenticado, escrita bloqueada (as fichas vêm do professor).
- `users/ana` e as subcoleções: leitura e escrita para quem está autenticado.
- Todo o resto fica negado.

> ⚠️ **Hoje qualquer usuário anônimo lê e altera os dados da "ana".** Isso serve para a demonstração, mas não para produção.

Quando o login existir, troque os blocos de `users/ana` por:

```
match /users/{uid}/{documento=**} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}
```

Nas fichas, use `allow read: if resource.data.alunoId == request.auth.uid;`.

Para o seed gravar as fichas, abra a escrita temporariamente. O passo a passo está no fim do próprio arquivo de regras.

---

## 6. Telas

| Tela | O que faz | Observação |
|---|---|---|
| **Splash** (`ui/splash`) | login anônimo → seed (em debug) → preferências → Onboarding ou Main | Mostra o logo por no mínimo 1,2 s. Se falhar, mostra "Tentar novamente". O antigo `activity_main.xml` virou `activity_splash.xml`. |
| **Onboarding** (`ui/onboarding`) | ViewPager2 com 3 slides | Aparece só na primeira vez (flag `onboarding_visto` no SharedPreferences). |
| **Main** (`ui/main`) | cabeçalho com o sino e badge de não lidas em tempo real, menu inferior com Fragments | Só "Treinos" e "Perfil" têm Fragment. As outras abas mostram `EmBreveFragment`. |
| **Meus Treinos** (`ui/treinos/TreinosFragment`) | semana atual de segunda a domingo (Grid de 7 colunas) e as fichas do dia | A semana é calculada a partir de hoje, sem datas fixas. |
| **Detalhe do Treino** (`ui/detalhetreino`) | resumo, exercícios ordenados, os concluídos hoje e "Iniciar treino" | Quando há treino em andamento, o botão vira "Treino em andamento". |
| **Notificações** (`ui/notificacoes`) | lista em tempo real agrupada em Hoje / Ontem / dd/MM | O RecyclerView substituiu o NestedScrollView. O listener é removido no `onStop`. |
| **Acessibilidade** (`ui/acessibilidade`) | switches salvos no aparelho e no Firestore, com uma prévia ao vivo | O switch do VLibras só salva a opção. A integração do VLibras não foi feita. |

**As fotos** ([Fotos.kt](app/src/main/java/com/example/unifit/util/Fotos.kt)) são fixas por posição (A, B, C) e não há campo `imagemUrl`. Isso evitou depender do Storage e do Glide, que estavam fora da lista de libs permitidas. **Para substituir:** adicione `imagemUrl` em `Ficha` e carregue a imagem no `FichaAdapter`, usando o drawable atual como placeholder.

Os botões fora do escopo (Nova ficha, Dica, Progresso, Histórico, Play do exercício, Mais opções) mostram um Snackbar "Em breve". Para ligar um deles, troque o `mostrarMensagem(R.string.em_breve)` correspondente por um `startActivity(...)`.

---

## 7. Acessibilidade e tema

- [BaseActivity.kt](app/src/main/java/com/example/unifit/ui/base/BaseActivity.kt) é a base de todas as telas:
  - **Fonte maior:** multiplica o `fontScale` por 1,2.
  - **Alto contraste:** aplica `Theme.UniFit.AltoContraste` antes do `super.onCreate`.
  - Se a opção mudar em outra tela, a tela atual se recria ao voltar.
- [attrs.xml](app/src/main/res/values/attrs.xml) e [themes.xml](app/src/main/res/values/themes.xml): os XMLs do escopo usam `?attr/corTextoPrincipal` e similares em vez de `@color/...`. O tema normal e o de alto contraste definem as cores.
- [PreferenciasLocais.kt](app/src/main/java/com/example/unifit/util/PreferenciasLocais.kt) guarda uma cópia local, porque as opções precisam ser lidas na hora, antes de a tela existir, e o Firestore é assíncrono. Usa o mesmo arquivo e as mesmas chaves que a TelaBase antiga, então as telas antigas continuam enxergando os valores.

**Por quê:** o alto contraste por tema pinta a tela inteira de forma consistente, sem varrer as views uma a uma.

**Como substituir:**
- Para mudar as cores do alto contraste, edite só `Theme.UniFit.AltoContraste`.
- Para mudar o tamanho da fonte maior, troque `ESCALA_FONTE_MAIOR`.
- Para criar uma cor nova, crie o atributo em `attrs.xml` e defina ele **nos dois temas**. Se faltar em um, aparece magenta, de propósito.

---

## 8. Telas antigas (dos colegas)

Elas foram mexidas o mínimo possível, só o necessário para compilar e para a acessibilidade funcionar:
- **[TelaBase.kt](app/src/main/java/com/example/unifit/TelaBase.kt)** agora herda da `BaseActivity` e ganha a fonte maior e a recriação ao mudar a opção. Ela continua com o alto contraste antigo, que escurece textos, porque essas telas usam cores fixas e o tema escuro deixaria tudo ilegível. Isso é controlado por `usaTemaDeAltoContraste = false`.
- Algumas Activities (Home, Aulas, Perfil e outras) passaram a herdar de `TelaBase` ou tiveram os imports ajustados para os novos pacotes. São 1 ou 2 linhas por arquivo.
- [MenuInferior.kt](app/src/main/java/com/example/unifit/MenuInferior.kt): a aba Treinos agora abre a `ui.main.MainActivity`.
- O `AndroidManifest` aponta para as novas classes, e a Splash virou a tela de entrada.

**Como substituir:** quando uma tela antiga trocar as cores fixas por `?attr/...`, basta tirar o override `usaTemaDeAltoContraste = false` dela e ela passa a usar o tema completo.

---

## 9. Pendências e alertas

1. **Trocar as regras** antes de qualquer uso real (seção 5).
2. **Fechar a escrita em `fichas`** depois de rodar o seed.
3. **Para compilar pelo terminal:** use `JAVA_HOME="C:/Program Files/Java/jdk-25.0.2"` e, se o `google-services.json` estiver faltando, `-x processDebugGoogleServices`.

---

# Parte 2 — Componentes do Android usados

Para cada componente: o que é, onde está, por que foi usado e como trocar.

## 1. View Binding

**O que é:** o Android gera uma classe por XML (`activity_main.xml` → `ActivityMainBinding`) com uma propriedade para cada `android:id`.

**Onde:** em todas as Activities, Fragments e ViewHolders.

```kotlin
binding = ActivityMainBinding.inflate(layoutInflater)
setContentView(binding.root)
binding.btnNotificacoes.setOnClickListener { ... }
```

**Por quê:** substitui o `findViewById`. Não tem cast, não quebra com id errado (o erro aparece ao compilar) e já estava ligado no projeto (`viewBinding = true`).

**Como trocar:** dá para voltar a `findViewById<TextView>(R.id.txtX)`, mas não compensa. Ao renomear um id no XML, o nome no binding muda junto (`txt_nome` vira `txtNome`).

---

## 2. Activity e BaseActivity

**O que é:** cada tela inteira é uma Activity. A [BaseActivity.kt](app/src/main/java/com/example/unifit/ui/base/BaseActivity.kt) é uma classe-mãe que concentra o que todas as telas fazem igual.

**Ciclo de vida usado:**

| Método | Para que foi usado |
|---|---|
| `attachBaseContext` | aplicar a fonte maior, que precisa acontecer antes de a tela existir |
| `onCreate` | `setTheme` do alto contraste (antes do `super`), inflar o layout e ligar os cliques |
| `onStart` / `onStop` | ligar e desligar os listeners em tempo real do Firestore (badge e notificações) |
| `onResume` | detectar que a acessibilidade mudou e chamar `recreate()` |
| `onDestroy` | cancelar o `Handler` da Splash e o callback do ViewPager2 |

**Por quê:** sem a classe-mãe, cada tela repetiria a lógica de fonte, tema e barras do sistema.

**Como trocar:** para uma tela nova, herde de `BaseActivity` (ou de `TelaBase`, se ela usa cores fixas). Para desligar algo numa tela só, sobrescreva `usaTemaDeAltoContraste` ou `configuraBarrasDoSistema`.

---

## 3. Edge-to-edge e WindowInsets

**O que é:** o app desenha atrás da barra de status e da barra de navegação. O `WindowInsets` informa quanto espaço elas ocupam.

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
    val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
    insets
}
```

**Onde:** em todas as telas. Na `MainActivity` o listener retorna `WindowInsetsCompat.CONSUMED`, porque senão o `BottomNavigationView` somaria o espaço da barra de navegação de novo.

**Por quê:** a partir do Android 15 o edge-to-edge é obrigatório. A BaseActivity também escolhe ícones claros ou escuros na barra de status (`SystemBarStyle.dark/light`).

**Como trocar:** se uma tela ficar com conteúdo embaixo da barra de status, confira se o `android:id="@+id/main"` está na raiz do XML.

---

## 4. Fragment e FragmentContainerView

**O que é:** um Fragment é um pedaço de tela com ciclo de vida próprio, que vive dentro de uma Activity. O `FragmentContainerView` em [activity_main.xml](app/src/main/res/layout/activity_main.xml) é a área onde ele aparece.

**Onde:** `TreinosFragment`, `PerfilFragment` e `EmBreveFragment`, trocados pela `MainActivity`:

```kotlin
supportFragmentManager.beginTransaction()
    .replace(R.id.containerAbas, fragment)
    .commit()
```

**Por quê:** o cabeçalho (logo, título e sino com badge) e o menu inferior ficam fixos, e só o meio troca. Antes, cada aba era uma Activity que repetia cabeçalho e menu e "piscava" ao trocar.

**Padrões que valem saber:**
- **`_binding` anulável:**
  ```kotlin
  private var _binding: FragmentTreinosBinding? = null
  private val binding get() = _binding!!
  override fun onDestroyView() { _binding = null }
  ```
  A View do Fragment morre antes do próprio Fragment. Segurar o binding depois disso vaza memória. Pelo mesmo motivo, as respostas do Firestore checam `if (_binding == null) return`.
- **`onCreateView` × `onViewCreated`:** o primeiro só infla o layout. O segundo configura listas e cliques.
- **Rotação de tela:** quando `savedInstanceState != null`, o `FragmentManager` já recriou o Fragment sozinho, então a `MainActivity` só restaura o título.
- **Fragment falando com a Activity:** `(activity as? MainActivity)?.mostrarMensagem(...)` mostra o Snackbar acima do menu.

**Como trocar:**
- Para ligar uma aba "Em breve", crie um Fragment, por exemplo `AulasFragment`, e mude o `when` em `mostrarAba()`: `R.id.nav_aulas -> AulasFragment()`.
- Se o Navigation Component for liberado um dia, ele substitui esse `replace` manual. Hoje está fora da lista permitida.

---

## 5. BottomNavigationView e BadgeDrawable

**O que é:** o menu inferior do Material Components, com o menu em `@menu/menu_bottom_nav`. O `BadgeDrawable` é a bolinha com número no sino.

**Onde:** na [MainActivity.kt](app/src/main/java/com/example/unifit/ui/main/MainActivity.kt).
- `setOnItemSelectedListener` troca o Fragment.
- `setOnItemReselectedListener { }` faz com que tocar na aba atual não recrie o Fragment.
- `BadgeUtils.attachBadgeDrawable(badge, binding.btnNotificacoes)` roda dentro de `post { }`, porque o badge precisa do tamanho final do sino.

**Por quê:** o sino não é um item do BottomNavigation, então o badge precisou ser preso manualmente. A `contentDescription` muda para "Notificações, 3 não lidas", que é o que o leitor de tela anuncia.

**Como trocar:** `horizontalOffset` e `verticalOffset` controlam a posição da bolinha. A cor vem de `colorError` do tema.

---

## 6. RecyclerView

A peça mais usada. Aparece em 4 listas:

| Lista | Adapter | LayoutManager | Item XML |
|---|---|---|---|
| Dias da semana | `DiaAdapter` | `GridLayoutManager(7)` | `item_dia.xml` |
| Fichas do dia | `FichaAdapter` | `LinearLayoutManager` | `item_ficha.xml` |
| Exercícios | `ExercicioAdapter` | `LinearLayoutManager` | `item_exercicio.xml` |
| Notificações | `NotificacaoAdapter` | `LinearLayoutManager` (no XML) | `item_notificacao.xml` + `item_cabecalho_data.xml` |

### 6.1 As 3 partes
1. **RecyclerView (XML):** só a área da lista.
2. **LayoutManager:** como os itens se organizam (lista vertical ou grade).
3. **Adapter e ViewHolder:** o Adapter cria as linhas e o ViewHolder preenche uma linha (`bind`).

**Por quê:** antes as listas eram `LinearLayout`s com itens repetidos no XML, ou montados com `addView`. O RecyclerView reaproveita as linhas que saem da tela e recebe dados de tamanho variável vindos do Firestore.

### 6.2 ListAdapter e DiffUtil

```kotlin
class FichaAdapter(...) : ListAdapter<FichaItem, FichaViewHolder>(Diferenca)

private object Diferenca : DiffUtil.ItemCallback<FichaItem>() {
    override fun areItemsTheSame(a, b) = a.ficha.id == b.ficha.id  // é o mesmo item?
    override fun areContentsTheSame(a, b) = a == b                 // mudou algo nele?
}
```

- A tela só chama `adapter.submitList(novaLista)`. O DiffUtil calcula o que mudou e redesenha só essas linhas, com animação.
- Isso combina com o Firestore em tempo real: quando uma notificação é marcada como lida, só aquela linha muda. No seletor de dias, só mudam o dia antigo e o novo selecionado.

**Como trocar:** um `RecyclerView.Adapter` comum com `notifyDataSetChanged()` também funciona, mas redesenha tudo e perde as animações.

### 6.3 Modelos de tela (wrappers)

Os adapters não recebem o `model` puro. Recebem uma classe com o que a tela precisa a mais:

| Classe | Embrulha | Acrescenta |
|---|---|---|
| `DiaSemana` | `LocalDate` | `selecionado`, `hoje` |
| `FichaItem` | `Ficha` | `hoje` (etiqueta), `foto` (pela posição) |
| `ExercicioLinha` | `ItemExercicio` | `concluido` (vem do Progresso) |
| `LinhaNotificacao` | `Notificacao` | é cabeçalho ou item, `tempo` ("há 10 min") |

**Por quê:** o `model` espelha o documento do Firestore. Misturar estado de tela nele faria o `toObject()` tentar ler campos que não existem no banco.

### 6.4 Clique por lambda

```kotlin
FichaAdapter { ficha -> abrirDetalhe(ficha) }
```

O adapter não sabe o que acontece no clique, só avisa. Quem decide é a tela. Para mudar a ação, troque a lambda sem mexer no adapter.

### 6.5 Vários tipos de linha (Notificações)

```kotlin
sealed interface LinhaNotificacao {
    data class Cabecalho(val rotulo: String) : LinhaNotificacao
    data class Item(val notificacao: Notificacao, val tempo: String) : LinhaNotificacao
}
override fun getItemViewType(position: Int) = when (getItem(position)) { ... }
```

- O cabeçalho ("HOJE", "ONTEM", "15/09") é uma linha da própria lista. Quem insere é a `montarLinhas()` da Activity, sempre que o dia muda.
- O `sealed` obriga o `when` a tratar os dois casos.

**Como trocar:** para um terceiro tipo (um banner, por exemplo), crie mais uma classe no `sealed`, mais um `TIPO_`, mais um ViewHolder e um XML.

### 6.6 Detalhes que evitam bugs
- **Grade de 7 colunas** em vez de `LinearLayout` com `layout_weight`, porque `weight` não funciona dentro do RecyclerView. Por isso o `item_dia.xml` tem largura `match_parent`: o Grid divide a largura em 7.
- **`nestedScrollingEnabled="false"`** em `rvDias`, `rvFichas` e `rvExercicios`, porque eles ficam dentro de um `NestedScrollView`. A rolagem é da tela toda, não da lista.
- **Os dois estados sempre definidos no `bind`** (lida e não lida, por exemplo), porque o ViewHolder é reaproveitado. Se só um estado fosse definido, uma linha herdaria a cor da anterior.
- **`tools:listitem` e `tools:itemCount`** no XML servem só para o preview do Android Studio e não vão para o app.

---

## 7. ViewPager2 (Onboarding)

**O que é:** um carrossel de páginas que usa um adapter de RecyclerView por baixo.

**Onde:** na [OnboardingActivity.kt](app/src/main/java/com/example/unifit/ui/onboarding/OnboardingActivity.kt), com o [OnboardingAdapter.kt](app/src/main/java/com/example/unifit/ui/onboarding/OnboardingAdapter.kt) e o `item_onboarding.xml`.
- **`SlideOnboarding`** guarda só ids de recurso (`@DrawableRes`, `@StringRes`). Os textos ficam no `strings.xml`.
- **Adapter simples** (`RecyclerView.Adapter`, não `ListAdapter`), porque a lista é fixa e nunca muda.
- **`OnPageChangeCallback`** atualiza as bolinhas e o texto do botão ("Próximo" ou "Começar") tanto no toque quanto no arrasto. É removido no `onDestroy`.
- **Indicadores:** 3 `View`s cuja largura muda em código (24dp ativa, 8dp inativa).

**Como trocar:**
- Para um 4º slide, adicione um item em `slides`, as strings e uma 4ª `View` de indicador no XML, e inclua ela na lista de `atualizarIndicadores`.
- Para gerar as bolinhas automaticamente, use `TabLayout` com `TabLayoutMediator`, que faz parte do Material e é permitido.

---

## 8. Layouts XML

| Layout | Raiz | Por quê |
|---|---|---|
| `activity_main.xml` | `ConstraintLayout` | cabeçalho preso no topo, menu embaixo, Fragment no meio com `0dp` (ocupa o que sobrar) |
| `fragment_treinos.xml` | `NestedScrollView` › `LinearLayout` vertical | conteúdo empilhado que rola inteiro |
| `activity_notificacoes.xml` | `ConstraintLayout` + RecyclerView | o RecyclerView já rola sozinho. Ele substituiu o `NestedScrollView` |
| `activity_splash.xml` | antigo `activity_main.xml` renomeado | o nome `activity_main` ficou para a tela principal |
| `item_*.xml` | uma linha de cada lista | o ViewHolder infla com `inflate(inflater, parent, false)` |
| `fragment_em_breve.xml`, `fragment_perfil.xml` | placeholders simples | — |

**Convenções seguidas:**
- Os elementos novos estão marcados com `<!-- NOVO: ... -->` no XML, para mostrar o que mudou em relação ao protótipo.
- Cada lista vem com um `ProgressBar` (carregando) e um `TextView` de vazio ("Nenhuma ficha neste dia"). A tela alterna a `visibility` entre lista, carregando e vazio.
- `?attr/selectableItemBackground` no `foreground` dá o efeito de toque.
- `contentDescription` nos ícones clicáveis e `importantForAccessibility="no"` nos decorativos.
- `accessibilityHeading="true"` nos cabeçalhos de data, para o leitor de tela pular por seção.

---

## 9. Temas, atributos, drawables e seletores

**Atributos ([attrs.xml](app/src/main/res/values/attrs.xml)):** são "variáveis de cor". O XML diz `?attr/corDestaque`, e o tema ativo decide se é azul (normal) ou amarelo (alto contraste).

**Drawables com `?attr`:** `bg_card_tema`, `bg_dia_selecionado`, `bg_chip` e outros usam `?attr/corCard` e similares.
- O `bg_card_tema` é uma **cópia** do `bg_card_branco`, porque o original também é usado nas telas antigas, que têm cores fixas.

**Seletor de cor ([cor_item_bottom_nav_tema.xml](app/src/main/res/color/cor_item_bottom_nav_tema.xml)):** cor por estado (`state_checked`) para o item ativo do menu.

**No código ([Tema.kt](app/src/main/java/com/example/unifit/util/Tema.kt)):** `Tema.cor(context, R.attr.corDestaque)`, usado no `DiaAdapter` e no `NotificacaoAdapter` para pintar em código seguindo o tema. Se o atributo faltar, a cor sai magenta, de propósito, para o erro ser visível.

**Como trocar:** para uma cor nova, crie o `<attr>`, defina ele em `Theme.UniFit` e em `Theme.UniFit.AltoContraste`, e use `?attr/` no XML.

---

## 10. Recursos de texto

| Tipo | Exemplo | Por quê |
|---|---|---|
| `string` com parâmetro | `fichas_ativas_dia` → "Fichas Ativas • %1$s" | nada de texto fixo no Kotlin |
| `plurals` | `ficha_qtd_exercicios` → "1 exercício" / "8 exercícios" | singular e plural corretos |
| `string-array` | `dias_semana_sigla`, `_curto`, `_completo` | índice = `dayOfWeek.value - 1` |

**Como trocar:** os textos mudam só no [strings.xml](app/src/main/res/values/strings.xml). Para traduzir, crie `values-en/strings.xml`.

---

## 11. Mensagens: Snackbar e Toast

**[Mensagens.kt](app/src/main/java/com/example/unifit/util/Mensagens.kt):** `Snackbar` com `setAnchorView` aparece acima do menu inferior ou do botão "Iniciar treino", sem cobri-los.
- **Toast** só no `DetalheTreinoActivity` quando a ficha é inválida, porque a tela fecha na hora e um Snackbar morreria junto.
- **Snackbar `LENGTH_INDEFINITE` com ação** "Tentar novamente", usado no erro ao carregar a ficha.

---

## 12. Navegação entre telas: Intent e extras

```kotlin
intent.putExtra(Constantes.EXTRA_FICHA_ID, ficha.id)        // envia
intent.getStringExtra(Constantes.EXTRA_FICHA_ID)            // recebe
```

- **Passa só o id, não a ficha inteira.** O detalhe busca a ficha no Firestore e sempre mostra o dado atual.
- **`finish()`** na Splash impede que o botão voltar retorne a ela.
- **`finishAffinity()`** no fim do Onboarding fecha todas as telas antes de abrir a Main.

---

## 13. Handler e postDelayed (Splash)

```kotlin
handler.postDelayed(abrirProximaTela, (TEMPO_MINIMO_MS - decorrido).coerceAtLeast(0))
```

Não é um atraso fixo. Ele garante só um mínimo de 1,2 s de logo: se o Firebase demorar mais, a navegação acontece na hora. O `removeCallbacks` no `onDestroy` evita abrir a próxima tela se o usuário já fechou o app.

**Como trocar:** a API oficial `SplashScreen` (`androidx.core:core-splashscreen`) não estava na lista permitida.

---

## 14. SharedPreferences

**[PreferenciasLocais.kt](app/src/main/java/com/example/unifit/util/PreferenciasLocais.kt):** guarda a flag do onboarding e uma cópia local da acessibilidade.

**Por quê:** a `BaseActivity` precisa saber da fonte e do tema **antes** de desenhar a tela, e o Firestore é assíncrono. O fluxo fica assim:
1. A Splash busca no Firestore.
2. Salva no SharedPreferences.
3. As telas leem do SharedPreferences.

Usa o mesmo arquivo (`"unifit"`) e as mesmas chaves da TelaBase antiga, então as telas dos colegas continuam funcionando.

---

## 15. Padrões do Firestore usados

| Padrão | Onde | Por quê |
|---|---|---|
| `get()` + `addOnSuccessListener/FailureListener` | fichas, progresso, preferências | leitura única. Callbacks, porque Coroutines estão fora da lista |
| `addSnapshotListener` + `ListenerRegistration.remove()` | notificações e badge | tempo real. Ligado no `onStart` e removido no `onStop` para não gastar leituras com a tela fechada |
| `toObject(Ficha::class.java)` + `@DocumentId` | models | converte o documento em classe. O `@DocumentId` preenche o `id` sem gravá-lo como campo |
| `whereEqualTo` + `whereArrayContains` | fichas do dia | o filtro `diasSemana contém 3` |
| `set(..., SetOptions.merge())` + `FieldValue.serverTimestamp()` | iniciar treino | cria ou atualiza sem apagar `exerciciosConcluidos`. O horário vem do servidor, não do celular |
| `update("lida", true)` | marcar uma | altera um campo só |
| `WriteBatch` | marcar todas, seed | várias escritas atômicas (ou tudo ou nada) |

**Proteções nas respostas assíncronas:**
- **`if (isFinishing || isDestroyed) return`** nas Activities: a resposta pode chegar depois de a tela fechar.
- **`if (_binding == null || dia != diaSelecionado) return`** no Fragment: ignora a resposta de um dia em que o usuário já não está mais. Isso evita mostrar as fichas de terça quando ele já tocou em quarta.

---

# Mapa rápido: "quero mudar X"

| Quero… | Mexo em… |
|---|---|
| uma aba nova de verdade | um Fragment novo + o `when` de `mostrarAba()` na `MainActivity` |
| um campo novo na ficha | `model/Ficha.kt` + `FichaAdapter.bind()` + `item_ficha.xml` + `SeedDebug` |
| o visual de uma linha | o `item_*.xml` correspondente (mantendo os ids) |
| a ação de um clique | a lambda passada ao adapter na tela |
| uma cor | o `themes.xml` (os dois temas) |
| um texto | o `strings.xml` |
| sair do usuário "ana" | `Constantes.USUARIO_DEMO_ID` / `FirebaseHelper.usuarioDemo` + as regras |
| o banco de dados | só a pasta `data/` |
