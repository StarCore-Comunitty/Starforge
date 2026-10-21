@file:OptIn(ExperimentalMaterial3Api::class)

package com.starcore.starforge

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

// =====================================================================
//  PALETA STARFORGE
// =====================================================================
private val Ink = Color(0xFF0F1115)          
private val Navy = Color(0xFF1A2B4C)         
private val NavyDeep = Color(0xFF111D36)     
private val Slate = Color(0xFF3C3F46)        
private val Gold = Color(0xFFF4C430)         
private val OnDark = Color(0xFFF1F5F9)
private val OnDarkMuted = Color(0xFFA9B3C6)
private val Hairline = Color(0xFF3B4658)
private val DangerRed = Color(0xFFFF6B6B)

private data class ThemeOption(val name: String, val color: Color)

private val THEMES = listOf(
    ThemeOption("Oro", Gold),
    ThemeOption("Zafiro", Color(0xFF6EA8FF)),
    ThemeOption("Esmeralda", Color(0xFF3DDC97))
)

private const val XP_HABIT = 10
private const val XP_PER_LEVEL = 100

private val QUOTES = listOf(
    "La disciplina de hoy es la libertad de mañana.",
    "No esperes motivación: construye el hábito y la motivación te seguirá.",
    "Los pequeños pasos de cada día superan a los grandes planes que nunca empiezan.",
    "Eres el resultado de lo que repites. Repite algo grande.",
    "Hoy no tiene que ser perfecto, solo tiene que contar.",
    "Cuando se ponga difícil, recuerda por qué empezaste.",
    "La constancia silenciosa vence al talento que se rinde.",
    "Cada hábito cumplido es un voto por la persona que quieres ser.",
    "Enfócate en el siguiente paso, no en toda la escalera.",
    "Tu futuro se decide en lo que haces cuando nadie mira.",
    "Hazlo con calma, pero hazlo hoy.",
    "Mejorar un 1 % cada día cambia por completo un año.",
    "La fuerza no está en no caer, sino en levantarte una vez más.",
    "Lo difícil de hoy será tu normalidad de mañana."
)

// =====================================================================
//  TEMA MATERIAL 3
// =====================================================================
private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.2.sp)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
private fun StarforgeTheme(accent: Color, content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = accent,
        onPrimary = Ink,
        primaryContainer = Navy,
        onPrimaryContainer = OnDark,
        secondary = accent,
        onSecondary = Ink,
        secondaryContainer = Slate,
        onSecondaryContainer = OnDark,
        background = Ink,
        onBackground = OnDark,
        surface = Ink,
        onSurface = OnDark,
        surfaceVariant = Slate,
        onSurfaceVariant = OnDarkMuted,
        outline = Hairline,
        error = DangerRed
    )
    MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
}

// =====================================================================
//  FECHAS
// =====================================================================
private object Dates {
    private val WEEKDAYS_SHORT = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    private val WEEKDAYS_LONG = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    private val MONTHS = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")

    fun keyOf(cal: Calendar): String = String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    fun keyOf(millis: Long): String = keyOf(Calendar.getInstance().apply { timeInMillis = millis })
    fun today(): String = keyOf(Calendar.getInstance())

    fun parse(key: String): Calendar {
        val p = key.split("-")
        return Calendar.getInstance().apply {
            clear()
            set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt(), 12, 0, 0)
        }
    }

    fun addDays(key: String, days: Int): String {
        val cal = parse(key)
        cal.add(Calendar.DAY_OF_YEAR, days)
        return keyOf(cal)
    }

    fun weekdayIndex(key: String): Int = (parse(key).get(Calendar.DAY_OF_WEEK) + 5) % 7
    fun weekStart(key: String): String = addDays(key, -weekdayIndex(key))
    fun dayOfMonth(key: String): Int = parse(key).get(Calendar.DAY_OF_MONTH)
    fun monthShort(key: String): String = MONTHS[parse(key).get(Calendar.MONTH)].take(3)
    fun weekdayShort(key: String): String = WEEKDAYS_SHORT[weekdayIndex(key)]
    fun weekdayLong(key: String): String = WEEKDAYS_LONG[weekdayIndex(key)]

    fun formatLong(key: String): String {
        val cal = parse(key)
        val text = "${weekdayLong(key)} ${cal.get(Calendar.DAY_OF_MONTH)} de ${MONTHS[cal.get(Calendar.MONTH)]}"
        return text.replaceFirstChar { it.uppercase() }
    }

    fun dayOfYear(): Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Buenos días"
            hour < 20 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }
}

private fun formatClock(ms: Long): String {
    val totalSeconds = ((ms + 999L) / 1000L).coerceAtLeast(0L)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

// =====================================================================
//  MODELO Y ESTADO
// =====================================================================
private data class Habit(val id: Long, val name: String, val doneDates: Set<String>)

private fun streakOf(habit: Habit, today: String): Int {
    var cursor = if (today in habit.doneDates) today else Dates.addDays(today, -1)
    var count = 0
    while (cursor in habit.doneDates) {
        count++
        cursor = Dates.addDays(cursor, -1)
    }
    return count
}

private class AppState(context: Context) {
    private val prefs = context.getSharedPreferences("starforge_prefs", Context.MODE_PRIVATE)

    val habits = mutableStateListOf<Habit>()
    var today by mutableStateOf(Dates.today())

    var userName by mutableStateOf("Juan")
    var userAge by mutableStateOf(18)
    var mission by mutableStateOf(DEFAULT_MISSION)
    var themeIndex by mutableStateOf(0)
    var xp by mutableStateOf(0)

    var focusMinutes by mutableStateOf(25)
    var timerEndAt by mutableStateOf(0L)        
    var timerPausedMs by mutableStateOf(0L)     
    var focusSessions by mutableStateOf(0)
    var focusTotalMinutes by mutableStateOf(0)
    var lastReward by mutableStateOf(0)         
    var nowMs by mutableStateOf(System.currentTimeMillis())

    val isIdle: Boolean get() = timerEndAt == 0L && timerPausedMs == 0L
    val level: Int get() = xp / XP_PER_LEVEL + 1
    val xpInLevel: Int get() = xp % XP_PER_LEVEL

    init { load() }

    private fun load() {
        userName = prefs.getString(K_NAME, "Juan") ?: "Juan"
        userAge = prefs.getInt(K_AGE, 18)
        mission = prefs.getString(K_MISSION, DEFAULT_MISSION) ?: DEFAULT_MISSION
        themeIndex = prefs.getInt(K_THEME, 0).coerceIn(0, THEMES.lastIndex)
        xp = prefs.getInt(K_XP, 0)
        focusMinutes = prefs.getInt(K_FOCUS_MIN, 25)
        timerEndAt = prefs.getLong(K_TIMER_END, 0L)
        timerPausedMs = prefs.getLong(K_TIMER_PAUSED, 0L)
        focusSessions = prefs.getInt(K_FOCUS_SESSIONS, 0)
        focusTotalMinutes = prefs.getInt(K_FOCUS_TOTAL, 0)
        habits.clear()
        habits.addAll(readHabits())
    }

    private fun readHabits(): List<Habit> = try {
        val arr = JSONArray(prefs.getString(K_HABITS, "[]") ?: "[]")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val doneArr = o.optJSONArray("done")
            val dates: Set<String> = if (doneArr == null) emptySet() else (0 until doneArr.length()).map { doneArr.getString(it) }.toSet()
            Habit(o.getLong("id"), o.getString("name"), dates)
        }
    } catch (e: Exception) { emptyList() }

    private fun saveHabits() {
        val arr = JSONArray()
        habits.forEach { h ->
            arr.put(JSONObject().put("id", h.id).put("name", h.name).put("done", JSONArray(h.doneDates.toList())))
        }
        prefs.edit().putString(K_HABITS, arr.toString()).apply()
    }

    private fun saveProfile() {
        prefs.edit().putString(K_NAME, userName).putInt(K_AGE, userAge).putString(K_MISSION, mission).putInt(K_THEME, themeIndex).apply()
    }

    private fun saveProgress() {
        prefs.edit().putInt(K_XP, xp).putInt(K_FOCUS_MIN, focusMinutes).putInt(K_FOCUS_SESSIONS, focusSessions).putInt(K_FOCUS_TOTAL, focusTotalMinutes).apply()
    }

    private fun saveTimer() {
        prefs.edit().putLong(K_TIMER_END, timerEndAt).putLong(K_TIMER_PAUSED, timerPausedMs).putInt(K_FOCUS_MIN, focusMinutes).apply()
    }

    fun statsFor(date: String): Pair<Int, Int> {
        if (date > today) return 0 to 0
        val relevant = habits.filter { Dates.keyOf(it.id) <= date }
        return relevant.count { date in it.doneDates } to relevant.size
    }

    fun bestStreak(): Int = habits.maxOfOrNull { streakOf(it, today) } ?: 0

    fun addHabit(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        habits.add(Habit(System.currentTimeMillis(), clean, emptySet()))
        saveHabits()
    }

    fun toggleHabit(id: Long) {
        val index = habits.indexOfFirst { it.id == id }
        if (index < 0) return
        val habit = habits[index]
        val newDates = if (today in habit.doneDates) {
            xp = (xp - XP_HABIT).coerceAtLeast(0)
            habit.doneDates - today
        } else {
            xp += XP_HABIT
            habit.doneDates + today
        }
        habits[index] = habit.copy(doneDates = newDates)
        saveHabits()
        saveProgress()
    }

    fun deleteHabit(id: Long) {
        habits.removeAll { it.id == id }
        saveHabits()
    }

    fun selectFocusMinutes(minutes: Int) {
        if (!isIdle) return
        focusMinutes = minutes
        saveTimer()
    }

    fun startFocus() {
        val now = System.currentTimeMillis()
        lastReward = 0
        timerPausedMs = 0L
        nowMs = now
        timerEndAt = now + focusMinutes * 60_000L
        saveTimer()
    }

    fun pauseFocus() {
        if (timerEndAt <= 0L) return
        val remaining = (timerEndAt - System.currentTimeMillis()).coerceAtLeast(1_000L)
        timerPausedMs = remaining
        timerEndAt = 0L
        saveTimer()
    }

    fun resumeFocus() {
        if (timerPausedMs <= 0L) return
        val now = System.currentTimeMillis()
        nowMs = now
        timerEndAt = now + timerPausedMs
        timerPausedMs = 0L
        saveTimer()
    }

    fun stopFocus() {
        timerEndAt = 0L
        timerPausedMs = 0L
        saveTimer()
    }

    fun completeFocus() {
        val minutes = focusMinutes
        timerEndAt = 0L
        timerPausedMs = 0L
        focusSessions += 1
        focusTotalMinutes += minutes
        xp += minutes
        lastReward = minutes
        saveTimer()
        saveProgress()
    }

    fun updateProfile(name: String, age: Int) {
        userName = name.trim().ifEmpty { "Juan" }
        userAge = age
        saveProfile()
    }

    fun updateMission(text: String) {
        mission = text.trim().ifEmpty { DEFAULT_MISSION }
        saveProfile()
    }

    fun updateTheme(index: Int) {
        themeIndex = index.coerceIn(0, THEMES.lastIndex)
        saveProfile()
    }

    fun resetProgress() {
        for (i in habits.indices) habits[i] = habits[i].copy(doneDates = emptySet())
        xp = 0
        focusSessions = 0
        focusTotalMinutes = 0
        lastReward = 0
        timerEndAt = 0L
        timerPausedMs = 0L
        saveHabits()
        saveProgress()
        saveTimer()
    }

    companion object {
        private const val K_NAME = "profile_name"
        private const val K_AGE = "profile_age"
        private const val K_MISSION = "profile_mission"
        private const val K_THEME = "profile_theme"
        private const val K_XP = "xp"
        private const val K_HABITS = "habits_v2"
        private const val K_FOCUS_MIN = "focus_minutes"
        private const val K_TIMER_END = "focus_timer_end"
        private const val K_TIMER_PAUSED = "focus_timer_paused"
        private const val K_FOCUS_SESSIONS = "focus_sessions"
        private const val K_FOCUS_TOTAL = "focus_total_minutes"
        const val DEFAULT_MISSION = "Construir disciplina diaria y convertirme en la mejor versión de mí."
    }
}

// =====================================================================
//  ACTIVITY
// =====================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val state = remember { AppState(applicationContext) }
            StarforgeTheme(accent = THEMES[state.themeIndex].color) {
                StarforgeApp(state)
            }
        }
    }
}

// =====================================================================
//  NAVEGACIÓN
// =====================================================================
private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("Inicio", Icons.Filled.Home),
    TabItem("Hábitos", Icons.Filled.Check),
    TabItem("Enfoque", Icons.Filled.Star),
    TabItem("Perfil", Icons.Filled.Person)
)

@Composable
private fun StarforgeApp(state: AppState) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var showAddHabit by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.timerEndAt) {
        while (state.timerEndAt > 0L) {
            val now = System.currentTimeMillis()
            state.nowMs = now
            if (now >= state.timerEndAt) {
                state.completeFocus()
                break
            }
            delay(200)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            val key = Dates.today()
            if (key != state.today) state.today = key
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { StarforgeBottomBar(selected = tab, onSelect = { tab = it }) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = tab == 1,
                enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionButton(
                    onClick = { showAddHabit = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Ink,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir hábito")
                }
            }
        }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(160)) },
                label = "tabs"
            ) { current ->
                when (current) {
                    0 -> HomeScreen(state, onOpenProfile = { tab = 3 })
                    1 -> HabitsScreen(state)
                    2 -> FocusScreen(state)
                    else -> ProfileScreen(state)
                }
            }
        }
    }

    if (showAddHabit) {
        AddHabitDialog(
            onDismiss = { showAddHabit = false },
            onAdd = {
                state.addHabit(it)
                showAddHabit = false
            }
        )
    }
}

@Composable
private fun StarforgeBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        color = Navy,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 10.dp
    ) {
        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
            TABS.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selected == index,
                    onClick = { onSelect(index) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Ink,
                        selectedTextColor = accent,
                        indicatorColor = accent,
                        unselectedIconColor = OnDarkMuted,
                        unselectedTextColor = OnDarkMuted
                    )
                )
            }
        }
    }
}

// =====================================================================
//  COMPONENTES
// =====================================================================
@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge, color = OnDark)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OnDarkMuted)
        }
        trailing()
    }
}

@Composable
private fun AccentBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 12.dp) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.10f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.7f), accent)))
        )
    }
}

@Composable
private fun Pill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val bg by animateColorAsState(if (selected) accent else Color.Transparent, tween(200), label = "pillBg")
    val fg by animateColorAsState(if (selected) Ink else OnDarkMuted, tween(200), label = "pillFg")
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .clip(shape)
            .background(bg)
            .border(1.dp, if (selected) accent else Hairline, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun Avatar(name: String, size: Dp, fontSize: TextUnit, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Navy, NavyDeep)))
            .border(2.dp, accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().take(1).uppercase().ifEmpty { "?" },
            color = accent,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    container: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = OnDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(label, color = OnDarkMuted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CheckBubble(checked: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val bg by animateColorAsState(if (checked) accent else Color.Transparent, tween(220), label = "checkBg")
    val ring by animateColorAsState(if (checked) accent else Color(0xFF4B5C7D), tween(220), label = "checkRing")
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = scaleIn(tween(180)) + fadeIn(tween(180)),
            exit = scaleOut(tween(120)) + fadeOut(tween(120))
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnDark,
    unfocusedTextColor = OnDark,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color(0xFF4B5C7D),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = OnDarkMuted,
    focusedPlaceholderColor = OnDarkMuted.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = OnDarkMuted.copy(alpha = 0.6f)
)

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    danger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Navy,
        titleContentColor = OnDark,
        textContentColor = OnDarkMuted,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (danger) DangerRed else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnDarkMuted) }
        }
    )
}

// =====================================================================
//  PESTAÑA 1: INICIO
// =====================================================================
@Composable
private fun HomeScreen(state: AppState, onOpenProfile: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val today = state.today
    var selected by rememberSaveable { mutableStateOf(today) }

    val weekStart = Dates.weekStart(selected)
    val weekEnd = Dates.addDays(weekStart, 6)
    val (done, total) = state.statsFor(selected)
    val fraction = if (total == 0) 0f else done.toFloat() / total
    val animatedFraction by animateFloatAsState(fraction, tween(700), label = "dayProgress")
    val percent = (fraction * 100).toInt()
    val quote = QUOTES[Dates.dayOfYear() % QUOTES.size]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(Dates.greeting(), color = OnDarkMuted, style = MaterialTheme.typography.bodyLarge)
                    Text("Hola, ${state.userName}", style = MaterialTheme.typography.headlineLarge, color = OnDark)
                    Text(Dates.formatLong(today), color = OnDarkMuted, style = MaterialTheme.typography.bodyMedium)
                }
                Avatar(name = state.userName, size = 52.dp, fontSize = 22.sp, modifier = Modifier.clickable(onClick = onOpenProfile))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Navy, contentColor = OnDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(22.dp)) {
                    Column {
                        Text(
                            if (selected == today) "Progreso de hoy" else "Progreso del ${Dates.weekdayLong(selected)} ${Dates.dayOfMonth(selected)}",
                            color = OnDarkMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("$percent%", color = accent, fontSize = 54.sp, fontWeight = FontWeight.Light)
                        Spacer(Modifier.height(10.dp))
                        AccentBar(animatedFraction, height = 12.dp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            when {
                                total == 0 -> "Aún no hay hábitos para este día"
                                done == total -> "¡Día perfecto! Todos los hábitos completados"
                                else -> "$done de $total hábitos completados"
                            },
                            color = OnDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Semana del ${Dates.dayOfMonth(weekStart)} ${Dates.monthShort(weekStart)} al ${Dates.dayOfMonth(weekEnd)} ${Dates.monthShort(weekEnd)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnDark
                    )
                    IconButton(onClick = { selected = Dates.addDays(selected, -7) }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, "Semana anterior", tint = OnDarkMuted)
                    }
                    IconButton(onClick = { selected = Dates.addDays(selected, 7) }) {
                        Icon(Icons.Filled.KeyboardArrowRight, "Semana siguiente", tint = OnDarkMuted)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0..6) {
                        val key = Dates.addDays(weekStart, i)
                        val (d, t) = state.statsFor(key)
                        DayChip(
                            dateKey = key,
                            isSelected = key == selected,
                            isToday = key == today,
                            done = d,
                            total = t,
                            modifier = Modifier.weight(1f),
                            onClick = { selected = key }
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val best = state.bestStreak()
                StatCard("Mejor racha", "$best ${if (best == 1) "día" else "días"}", Navy, Modifier.weight(1f))
                StatCard("Nivel ${state.level}", "${state.xp} XP", Slate, Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Slate, contentColor = OnDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("Frase del día", color = accent, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(10.dp))
                    Text("“$quote”", color = OnDark, fontSize = 19.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    dateKey: String,
    isSelected: Boolean,
    isToday: Boolean,
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val bg by animateColorAsState(if (isSelected) accent else Navy, tween(220), label = "dayBg")
    val textColor by animateColorAsState(if (isSelected) Ink else OnDark, tween(220), label = "dayText")
    val subColor by animateColorAsState(if (isSelected) Ink.copy(alpha = 0.7f) else OnDarkMuted, tween(220), label = "daySub")
    val shape = RoundedCornerShape(18.dp)
    val dotColor = when {
        total > 0 && done == total -> if (isSelected) Ink else accent
        done > 0 -> if (isSelected) Ink.copy(alpha = 0.45f) else accent.copy(alpha = 0.45f)
        else -> Color.Transparent
    }

    Column(
        modifier
            .clip(shape)
            .background(bg)
            .border(width = if (isToday && !isSelected) 1.5.dp else 0.dp, color = if (isToday && !isSelected) accent else Color.Transparent, shape = shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(Dates.weekdayShort(dateKey), color = subColor, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(Dates.dayOfMonth(dateKey).toString(), color = textColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
    }
}

// =====================================================================
//  PESTAÑA 2: HÁBITOS
// =====================================================================
@Composable
private fun HabitsScreen(state: AppState) {
    val accent = MaterialTheme.colorScheme.primary
    val today = state.today
    var filter by rememberSaveable { mutableStateOf(0) } 
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    val total = state.habits.size
    val doneCount = state.habits.count { today in it.doneDates }
    val pendingCount = total - doneCount
    val fraction by animateFloatAsState(if (total == 0) 0f else doneCount.toFloat() / total, tween(600), label = "habitsProgress")
    val shown = state.habits.filter {
        when (filter) {
            1 -> today !in it.doneDates
            2 -> today in it.doneDates
            else -> true
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        ScreenHeader(
            title = "Hábitos",
            subtitle = Dates.formatLong(today),
            trailing = {
                Box(Modifier.clip(RoundedCornerShape(50)).background(Navy).border(1.dp, accent, RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("$doneCount/$total", color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        AccentBar(fraction, height = 8.dp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Todos", filter == 0) { filter = 0 }
            Pill("Pendientes · $pendingCount", filter == 1) { filter = 1 }
            Pill("Hechos · $doneCount", filter == 2) { filter = 2 }
        }
        Spacer(Modifier.height(16.dp))

        if (shown.isEmpty()) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Slate, contentColor = OnDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (total == 0) "Aún no tienes hábitos" else "Nada por aquí", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(if (total == 0) "Toca el botón + para crear tu primer hábito." else "Cambia el filtro para ver el resto de tus hábitos.", color = OnDarkMuted, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 104.dp)) {
                items(shown, key = { it.id }) { habit ->
                    HabitCard(habit = habit, today = today, onToggle = { state.toggleHabit(habit.id) }, onDelete = { habitToDelete = habit })
                }
            }
        }
    }

    habitToDelete?.let { habit ->
        ConfirmDialog(
            title = "Eliminar hábito",
            message = "¿Quieres eliminar \"${habit.name}\" y todo su historial?",
            confirmText = "Eliminar",
            danger = true,
            onConfirm = {
                state.deleteHabit(habit.id)
                habitToDelete = null
            },
            onDismiss = { habitToDelete = null }
        )
    }
}

@Composable
private fun HabitCard(habit: Habit, today: String, onToggle: () -> Unit, onDelete: () -> Unit) {
    val done = today in habit.doneDates
    val streak = streakOf(habit, today)
    val container by animateColorAsState(if (done) Color(0xFF15233F) else Navy, tween(250), label = "habitBg")
    val shape = RoundedCornerShape(24.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = OnDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().clip(shape).clickable(onClick = onToggle)
    ) {
        Row(Modifier.padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            CheckBubble(checked = done)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    habit.name,
                    color = if (done) OnDark.copy(alpha = 0.6f) else OnDark,
                    textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        streak == 0 -> "Sin racha todavía"
                        streak == 1 -> "Racha de 1 día"
                        else -> "Racha de $streak días"
                    },
                    color = OnDarkMuted,
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar hábito", tint = OnDarkMuted)
            }
        }
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val canAdd = text.trim().isNotEmpty()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(150)
        runCatching { focusRequester.requestFocus() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Navy,
        titleContentColor = OnDark,
        textContentColor = OnDark,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Nuevo hábito", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 50) text = it },
                singleLine = true,
                placeholder = { Text("Ej. Leer 20 minutos") },
                shape = RoundedCornerShape(18.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canAdd) onAdd(text.trim()) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(onClick = { if (canAdd) onAdd(text.trim()) }, enabled = canAdd) {
                Text("Añadir", color = if (canAdd) MaterialTheme.colorScheme.primary else OnDarkMuted, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnDarkMuted) }
        }
    )
}

// =====================================================================
//  PESTAÑA 3: ENFOQUE
// =====================================================================
@Composable
private fun FocusScreen(state: AppState) {
    val accent = MaterialTheme.colorScheme.primary
    val running = state.timerEndAt > 0L
    val paused = state.timerPausedMs > 0L
    val idle = !running && !paused
    val totalMs = state.focusMinutes * 60_000L
    val remainingMs = when {
        running -> (state.timerEndAt - state.nowMs).coerceIn(0L, totalMs)
        paused -> state.timerPausedMs.coerceIn(0L, totalMs)
        else -> totalMs
    }
    val targetProgress = if (totalMs == 0L) 0f else remainingMs.toFloat() / totalMs
    val progress by animateFloatAsState(targetProgress, tween(durationMillis = 250, easing = LinearEasing), label = "timerProgress")

    val view = LocalView.current
    DisposableEffect(running) {
        view.keepScreenOn = running
        onDispose { view.keepScreenOn = false }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "Modo Enfoque", subtitle = "Silencia el ruido y concéntrate en lo importante")
        Spacer(Modifier.height(28.dp))

        TimerRing(
            progress = progress,
            timeText = formatClock(remainingMs),
            caption = when {
                running -> "Enfocado"
                paused -> "En pausa"
                else -> "Listo para empezar"
            }
        )
        Spacer(Modifier.height(28.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(15, 25, 45, 60).forEach { minutes ->
                Pill(
                    text = "${minutes}m",
                    selected = state.focusMinutes == minutes,
                    enabled = idle,
                    modifier = Modifier.weight(1f),
                    onClick = { state.selectFocusMinutes(minutes) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        when {
            idle -> Button(
                onClick = { state.startFocus() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Ink)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Iniciar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { if (running) state.pauseFocus() else state.resumeFocus() },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Ink)
                ) {
                    if (!running) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (running) "Pausar" else "Reanudar", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { state.stopFocus() },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, Hairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnDark)
                ) {
                    Text("Detener", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        AnimatedVisibility(
            visible = state.lastReward > 0,
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Navy, contentColor = OnDark),
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth().border(1.dp, accent, RoundedCornerShape(24.dp))
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡Sesión completada!", color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Ganaste +${state.lastReward} XP por tu concentración.", color = OnDark)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Sesiones", state.focusSessions.toString(), Navy, Modifier.weight(1f))
            StatCard("Minutos enfocados", state.focusTotalMinutes.toString(), Slate, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Text("Cada minuto de enfoque completado suma 1 XP.", color = OnDarkMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TimerRing(progress: Float, timeText: String, caption: String) {
    val accent = MaterialTheme.colorScheme.primary
    Box(Modifier.size(270.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val half = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawCircle(
                brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.14f), Color.Transparent), center = center, radius = size.minDimension / 2f),
                radius = size.minDimension / 2f
            )
            drawCircle(color = Navy.copy(alpha = 0.65f), radius = size.minDimension / 2f - stroke * 1.5f)
            drawArc(
                color = Slate.copy(alpha = 0.6f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(half, half),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(half, half),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(timeText, color = OnDark, fontSize = 56.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
            Text(caption, color = OnDarkMuted, fontSize = 14.sp)
        }
    }
}

// =====================================================================
//  PESTAÑA 4: PERFIL
// =====================================================================
private enum class ProfileDialog { None, EditProfile, EditMission, Reset }

@Composable
private fun ProfileScreen(state: AppState) {
    val accent = MaterialTheme.colorScheme.primary
    var dialog by remember { mutableStateOf(ProfileDialog.None) }
    val levelFraction by animateFloatAsState(state.xpInLevel.toFloat() / XP_PER_LEVEL, tween(600), label = "levelProgress")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Navy, contentColor = OnDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Navy, NavyDeep))).padding(24.dp)) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Avatar(state.userName, 92.dp, 38.sp)
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(state.userName, style = MaterialTheme.typography.headlineMedium, color = OnDark)
                            IconButton(onClick = { dialog = ProfileDialog.EditProfile }) {
                                Icon(Icons.Filled.Edit, "Editar perfil", tint = accent)
                            }
                        }
                        Text("${state.userAge} años · Nivel ${state.level}", color = OnDarkMuted, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(18.dp))
                        AccentBar(levelFraction, height = 10.dp)
                        Spacer(Modifier.height(8.dp))
                        Text("${state.xpInLevel} / $XP_PER_LEVEL XP para el nivel ${state.level + 1}", color = OnDarkMuted, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Navy, contentColor = OnDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(start = 22.dp, top = 14.dp, end = 10.dp, bottom = 22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Misión personal", color = accent, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { dialog = ProfileDialog.EditMission }) {
                            Icon(Icons.Filled.Edit, "Editar misión", tint = OnDarkMuted)
                        }
                    }
                    Text(state.mission, color = OnDark, fontSize = 18.sp, lineHeight = 26.sp, modifier = Modifier.padding(end = 12.dp))
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Slate, contentColor = OnDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("Tema de acento", style = MaterialTheme.typography.titleMedium, color = OnDark)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        THEMES.forEachIndexed { index, option ->
                            ThemeSwatch(option = option, selected = state.themeIndex == index, onClick = { state.updateTheme(index) })
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Hábitos activos", state.habits.size.toString(), Navy, Modifier.weight(1f))
                StatCard("Mejor racha", "${state.bestStreak()} d", Navy, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Sesiones de enfoque", state.focusSessions.toString(), Slate, Modifier.weight(1f))
                StatCard("Minutos enfocados", state.focusTotalMinutes.toString(), Slate, Modifier.weight(1f))
            }
        }

        item {
            OutlinedButton(
                onClick = { dialog = ProfileDialog.Reset },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, DangerRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reiniciar progreso", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    when (dialog) {
        ProfileDialog.EditProfile -> EditProfileDialog(
            initialName = state.userName,
            initialAge = state.userAge,
            onDismiss = { dialog = ProfileDialog.None },
            onSave = { name, age ->
                state.updateProfile(name, age)
                dialog = ProfileDialog.None
            }
        )
        ProfileDialog.EditMission -> EditMissionDialog(
            initial = state.mission,
            onDismiss = { dialog = ProfileDialog.None },
            onSave = {
                state.updateMission(it)
                dialog = ProfileDialog.None
            }
        )
        ProfileDialog.Reset -> ConfirmDialog(
            title = "Reiniciar progreso",
            message = "Se pondrán a cero tu XP, rachas, historial de hábitos y estadísticas de enfoque. Tu perfil y tu lista de hábitos se conservan.",
            confirmText = "Reiniciar",
            danger = true,
            onConfirm = {
                state.resetProgress()
                dialog = ProfileDialog.None
            },
            onDismiss = { dialog = ProfileDialog.None }
        )
        ProfileDialog.None -> Unit
    }
}

@Composable
private fun ThemeSwatch(option: ThemeOption, selected: Boolean, onClick: () -> Unit) {
    val ring by animateColorAsState(if (selected) OnDark else Color.Transparent, tween(200), label = "swatchRing")
    Column(
        Modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(50.dp).clip(CircleShape).border(3.dp, ring, CircleShape).padding(5.dp).clip(CircleShape).background(option.color),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(option.name, color = if (selected) OnDark else OnDarkMuted, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun EditProfileDialog(initialName: String, initialAge: Int, onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var ageText by rememberSaveable { mutableStateOf(initialAge.toString()) }
    val age = ageText.toIntOrNull()
    val valid = name.trim().isNotEmpty() && age != null && age in 1..120

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Navy,
        titleContentColor = OnDark,
        textContentColor = OnDark,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Editar perfil", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    singleLine = true,
                    label = { Text("Nombre") },
                    shape = RoundedCornerShape(18.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { v -> if (v.length <= 3 && v.all { it.isDigit() }) ageText = v },
                    singleLine = true,
                    label = { Text("Edad") },
                    shape = RoundedCornerShape(18.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (valid && age != null) onSave(name, age) }, enabled = valid) {
                Text("Guardar", color = if (valid) MaterialTheme.colorScheme.primary else OnDarkMuted, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnDarkMuted) }
        }
    )
}

@Composable
private fun EditMissionDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    val valid = text.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Navy,
        titleContentColor = OnDark,
        textContentColor = OnDark,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Misión personal", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 140) text = it },
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("¿Qué quieres lograr?") },
                supportingText = { Text("${text.length}/140", color = OnDarkMuted) },
                shape = RoundedCornerShape(18.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (valid) onSave(text) }, enabled = valid) {
                Text("Guardar", color = if (valid) MaterialTheme.colorScheme.primary else OnDarkMuted, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnDarkMuted) }
        }
    )
}
