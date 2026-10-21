package com.starcore.starforge

/*
 * Starforge — disciplina, hábitos y superación personal.
 *
 * Una sola actividad · Jetpack Compose + Material 3 · SharedPreferences.
 */

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Paleta y Temas de Acento
// ─────────────────────────────────────────────────────────────────────────────

private val Ink = Color(0xFF0F1115)
private val Navy = Color(0xFF1A2B4C)
private val Slate = Color(0xFF3C3F46)
private val Gold = Color(0xFFF4C430)
private val Sapphire = Color(0xFF4C8DFF)
private val Emerald = Color(0xFF2ED8A3)
private val Ruby = Color(0xFFFF5252)
private val Amethyst = Color(0xFFAB47BC)

private val TextMain = Color(0xFFF1F3F7)
private val TextMuted = Color(0xFFA3AAB8)

enum class AppTheme(val label: String, val accent: Color) {
    GOLD("Oro", Gold),
    SAPPHIRE("Zafiro", Sapphire),
    EMERALD("Esmeralda", Emerald),
    RUBY("Rubí", Ruby),
    AMETHYST("Amatista", Amethyst)
}

// ─────────────────────────────────────────────────────────────────────────────
// Constantes y SharedPreferences
// ─────────────────────────────────────────────────────────────────────────────

private const val PREFS_NAME = "starforge_prefs"
private const val K_NAME = "user_name"
private const val K_AGE = "user_age"
private const val K_MISSION = "user_mission"
private const val K_THEME = "theme"
private const val K_XP = "xp"
private const val K_HABITS = "habits_json"
private const val K_COMPLETIONS = "completions_json"
private const val K_FOCUS_LOG = "focus_log_json"
private const val K_FOCUS_MIN = "focus_minutes"
private const val K_FOCUS_END = "focus_end_at"
private const val K_UNLOCKED = "unlocked_badges_json"
private const val K_NOTES = "notes_json"

private const val XP_PER_HABIT = 10
private const val XP_PER_FOCUS_MINUTE = 2
private const val XP_PER_LEVEL = 100
private const val DEFAULT_MISSION = "Forjar disciplina cada día y convertirme en mi mejor versión."

private val FOCUS_OPTIONS = listOf(15, 25, 45, 60)
private val DAYS_SHORT = listOf("L", "M", "X", "J", "V", "S", "D")
private val DAYS_FULL = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
private val QUOTES = listOf(
    "Cada día que cumples contigo mismo forja a la persona que quieres ser.",
    "La constancia silenciosa vence al talento que se distrae.",
    "No esperes motivación: construye el hábito y ella te alcanzará.",
    "Un paso pequeño hoy pesa más que un plan perfecto mañana.",
    "Tu futuro se escribe con lo que repites, no con lo que prometes.",
    "La disciplina es una forma de respeto hacia tu yo del futuro.",
    "Forja tu mejor versión: un hábito a la vez."
)

// ─────────────────────────────────────────────────────────────────────────────
// Modelos
// ─────────────────────────────────────────────────────────────────────────────

data class Habit(
    val id: Long,
    val name: String,
    val created: String,
    val days: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Lunes a 7=Domingo
    val category: String = "General"
)

enum class BadgeCategory(val label: String, val emoji: String, val unit: String) {
    STREAK("Rachas", "🔥", "días"),
    XP("Experiencia", "⭐", "XP"),
    FOCUS("Enfoque", "🎯", "sesiones"),
    HABITS("Hábitos", "✅", "check-ins")
}

data class Achievement(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val target: Int
)

data class Metrics(
    val currentStreak: Int,
    val bestStreak: Int,
    val xp: Int,
    val sessions: Int,
    val checkIns: Int
) {
    fun valueFor(category: BadgeCategory): Int = when (category) {
        BadgeCategory.STREAK -> bestStreak
        BadgeCategory.XP -> xp
        BadgeCategory.FOCUS -> sessions
        BadgeCategory.HABITS -> checkIns
    }
}

val ACHIEVEMENTS: List<Achievement> = listOf(
    Achievement("streak_3", "🔥", "Chispa", "Racha de 3 días seguidos", BadgeCategory.STREAK, 3),
    Achievement("streak_7", "⚡", "Semana de Fuego", "Racha de 7 días seguidos", BadgeCategory.STREAK, 7),
    Achievement("streak_14", "🛡️", "Voluntad de Acero", "Racha de 14 días seguidos", BadgeCategory.STREAK, 14),
    Achievement("streak_30", "👑", "Maestro de la Constancia", "Racha de 30 días seguidos", BadgeCategory.STREAK, 30),
    Achievement("streak_60", "🏆", "Leyenda Imparable", "Racha de 60 días seguidos", BadgeCategory.STREAK, 60),
    Achievement("xp_100", "✨", "Primer Destello", "Acumula 100 XP", BadgeCategory.XP, 100),
    Achievement("xp_500", "🌟", "Aprendiz Forjador", "Acumula 500 XP", BadgeCategory.XP, 500),
    Achievement("xp_1000", "💎", "Cristal de Voluntad", "Acumula 1.000 XP", BadgeCategory.XP, 1000),
    Achievement("xp_2500", "🚀", "Ascenso Estelar", "Acumula 2.500 XP", BadgeCategory.XP, 2500),
    Achievement("xp_5000", "🌌", "Forjador de Estrellas", "Acumula 5.000 XP", BadgeCategory.XP, 5000),
    Achievement("focus_1", "🎯", "Primer Enfoque", "Completa 1 sesión de enfoque", BadgeCategory.FOCUS, 1),
    Achievement("focus_5", "🧠", "Mente Clara", "Completa 5 sesiones de enfoque", BadgeCategory.FOCUS, 5),
    Achievement("focus_10", "⏳", "Dueño del Tiempo", "Completa 10 sesiones de enfoque", BadgeCategory.FOCUS, 10),
    Achievement("focus_25", "🧘", "Monje Digital", "Completa 25 sesiones de enfoque", BadgeCategory.FOCUS, 25),
    Achievement("focus_50", "🔮", "Concentración Absoluta", "Completa 50 sesiones de enfoque", BadgeCategory.FOCUS, 50),
    Achievement("habit_1", "✅", "Primer Paso", "Marca tu primer hábito", BadgeCategory.HABITS, 1),
    Achievement("habit_25", "📈", "En Marcha", "Completa 25 hábitos", BadgeCategory.HABITS, 25),
    Achievement("habit_100", "💪", "Centenario", "Completa 100 hábitos", BadgeCategory.HABITS, 100),
    Achievement("habit_250", "🏅", "Hábito de Hierro", "Completa 250 hábitos", BadgeCategory.HABITS, 250),
    Achievement("habit_500", "🌠", "Imparable", "Completa 500 hábitos", BadgeCategory.HABITS, 500)
)

// ─────────────────────────────────────────────────────────────────────────────
// Almacenamiento centralizado (StarforgeStore)
// ─────────────────────────────────────────────────────────────────────────────

class StarforgeStore(private val prefs: SharedPreferences) {

    var userName by mutableStateOf(prefs.getString(K_NAME, "Juan") ?: "Juan")
        private set
    var userAge by mutableStateOf(prefs.getString(K_AGE, "18") ?: "18")
        private set
    var mission by mutableStateOf(prefs.getString(K_MISSION, DEFAULT_MISSION) ?: DEFAULT_MISSION)
        private set
    var theme by mutableStateOf(loadTheme())
        private set
    var xp by mutableStateOf(prefs.getInt(K_XP, 0))
        private set

    var focusMinutes by mutableStateOf(prefs.getInt(K_FOCUS_MIN, 25))
        private set
    var focusEndAt by mutableStateOf<Long?>(prefs.getLong(K_FOCUS_END, 0L).takeIf { it > 0L })
        private set
    var focusRemainingMs by mutableStateOf(focusMinutes * 60_000L)

    var selectedDate by mutableStateOf(LocalDate.now())
    var toast by mutableStateOf<String?>(null)

    val habits = mutableStateListOf<Habit>()
    val completions = mutableStateMapOf<String, Set<Long>>()
    val focusLog = mutableStateMapOf<String, List<Int>>()
    val unlocked = mutableStateMapOf<String, String>()
    val notes = mutableStateMapOf<String, String>()

    init {
        if (prefs.contains(K_HABITS)) {
            habits.addAll(loadHabits())
        } else {
            val today = LocalDate.now().toString()
            habits.add(Habit(1L, "Beber 2 litros de agua", today, listOf(1,2,3,4,5,6,7), "Salud"))
            habits.add(Habit(2L, "Leer 10 páginas", today, listOf(1,2,3,4,5,6,7), "Mente"))
            habits.add(Habit(3L, "Ejercicio 20 minutos", today, listOf(1,3,5), "Cuerpo"))
            saveHabits()
        }
        completions.putAll(loadCompletions())
        focusLog.putAll(loadFocusLog())
        unlocked.putAll(loadUnlocked())
        notes.putAll(loadNotes())
        evaluateBadges()
    }

    val level: Int get() = xp / XP_PER_LEVEL + 1
    val xpInLevel: Int get() = xp % XP_PER_LEVEL

    fun updateName(value: String) {
        userName = value.take(24)
        prefs.edit().putString(K_NAME, userName).apply()
    }

    fun updateAge(value: String) {
        if (value.length <= 2 && value.all { it.isDigit() }) {
            userAge = value
            prefs.edit().putString(K_AGE, userAge).apply()
        }
    }

    fun updateMission(value: String) {
        mission = value.take(140)
        prefs.edit().putString(K_MISSION, mission).apply()
    }

    fun updateTheme(value: AppTheme) {
        theme = value
        prefs.edit().putString(K_THEME, value.name).apply()
    }

    // Filtra hábitos creados y que correspondan al día de la semana (1=Lunes .. 7=Domingo)
    fun habitsFor(date: LocalDate): List<Habit> {
        val key = date.toString()
        val dayOfWeek = date.dayOfWeek.value
        return habits.filter { it.created <= key && dayOfWeek in it.days }
    }

    fun isDone(habitId: Long, date: LocalDate): Boolean =
        completions[date.toString()]?.contains(habitId) == true

    fun doneCount(date: LocalDate): Int = completions[date.toString()]?.size ?: 0

    fun canEdit(date: LocalDate): Boolean = !date.isAfter(LocalDate.now())

    fun noteFor(date: LocalDate): String = notes[date.toString()] ?: ""

    fun updateNote(date: LocalDate, text: String) {
        val key = date.toString()
        if (text.isBlank()) {
            notes.remove(key)
        } else {
            notes[key] = text.take(300)
        }
        saveNotes()
    }

    fun addHabit(name: String, selectedDays: List<Int>, category: String) {
        val clean = name.trim().take(40)
        if (clean.isEmpty()) return
        var id = System.currentTimeMillis()
        while (habits.any { it.id == id }) id++
        habits.add(Habit(id, clean, LocalDate.now().toString(), selectedDays, category))
        saveHabits()
    }

    fun deleteHabit(habit: Habit) {
        habits.removeAll { it.id == habit.id }
        for (key in completions.keys.toList()) {
            val ids = completions[key] ?: continue
            if (habit.id in ids) {
                val next = ids - habit.id
                if (next.isEmpty()) completions.remove(key) else completions[key] = next
            }
        }
        saveHabits()
        saveCompletions()
    }

    fun toggleHabit(habit: Habit, date: LocalDate) {
        if (!canEdit(date)) return
        val key = date.toString()
        val current = completions[key] ?: emptySet()
        if (habit.id in current) {
            val next = current - habit.id
            if (next.isEmpty()) completions.remove(key) else completions[key] = next
            changeXp(-XP_PER_HABIT)
        } else {
            completions[key] = current + habit.id
            changeXp(XP_PER_HABIT)
        }
        saveCompletions()
        evaluateBadges()
    }

    private fun changeXp(delta: Int) {
        xp = (xp + delta).coerceAtLeast(0)
        prefs.edit().putInt(K_XP, xp).apply()
    }

    fun selectFocusMinutes(minutes: Int) {
        if (focusEndAt != null) return
        focusMinutes = minutes
        focusRemainingMs = minutes * 60_000L
        saveFocusTimer()
    }

    fun startFocus() {
        if (focusEndAt != null) return
        focusEndAt = System.currentTimeMillis() + focusMinutes * 60_000L
        saveFocusTimer()
    }

    fun cancelFocus() {
        focusEndAt = null
        focusRemainingMs = focusMinutes * 60_000L
        saveFocusTimer()
    }

    fun finishFocus() {
        val minutes = focusMinutes
        focusEndAt = null
        focusRemainingMs = minutes * 60_000L
        saveFocusTimer()

        val key = LocalDate.now().toString()
        focusLog[key] = (focusLog[key] ?: emptyList()) + minutes
        saveFocusLog()
        val gained = minutes * XP_PER_FOCUS_MINUTE
        changeXp(gained)
        toast = "🎯 Sesión de $minutes min completada · +$gained XP"
        evaluateBadges()
    }

    fun sessionsOn(date: LocalDate): Int = focusLog[date.toString()]?.size ?: 0
    fun minutesOn(date: LocalDate): Int = focusLog[date.toString()]?.sum() ?: 0
    fun totalSessions(): Int = focusLog.values.sumOf { it.size }
    fun totalFocusMinutes(): Int = focusLog.values.sumOf { it.sum() }

    fun currentStreak(): Int {
        val today = LocalDate.now()
        var day = if (completions.containsKey(today.toString())) today else today.minusDays(1)
        var count = 0
        while (completions.containsKey(day.toString())) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun bestStreak(): Int {
        val dates = completions.keys
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .sorted()
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        for (d in dates) {
            run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
            if (run > best) best = run
            prev = d
        }
        return best
    }

    fun totalCheckIns(): Int = completions.values.sumOf { it.size }

    fun metrics(): Metrics = Metrics(
        currentStreak = currentStreak(),
        bestStreak = bestStreak(),
        xp = xp,
        sessions = totalSessions(),
        checkIns = totalCheckIns()
    )

    private fun evaluateBadges() {
        val m = metrics()
        val newly = ACHIEVEMENTS.filter {
            !unlocked.containsKey(it.id) && m.valueFor(it.category) >= it.target
        }
        if (newly.isEmpty()) return
        val today = LocalDate.now().toString()
        newly.forEach { unlocked[it.id] = today }
        saveUnlocked()
        toast = if (newly.size == 1) {
            "🏆 ¡Logro desbloqueado: ${newly[0].title}!"
        } else {
            "🏆 ¡${newly.size} logros desbloqueados!"
        }
    }

    fun globalSuccessRate(): Int {
        val start = habits.minOfOrNull { it.created }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return 0
        val today = LocalDate.now()
        var date = start
        var scheduledTotal = 0
        var doneTotal = 0
        while (!date.isAfter(today)) {
            val scheduled = habitsFor(date).size
            scheduledTotal += scheduled
            doneTotal += minOf(doneCount(date), scheduled)
            date = date.plusDays(1)
        }
        return if (scheduledTotal > 0) (doneTotal * 100f / scheduledTotal).roundToInt() else 0
    }

    fun exportData(): String {
        val obj = JSONObject()
        obj.put(K_NAME, userName)
        obj.put(K_XP, xp)
        obj.put(K_HABITS, prefs.getString(K_HABITS, "[]"))
        obj.put(K_COMPLETIONS, prefs.getString(K_COMPLETIONS, "{}"))
        obj.put(K_NOTES, prefs.getString(K_NOTES, "{}"))
        return obj.toString()
    }

    fun resetProgress() {
        habits.clear()
        completions.clear()
        focusLog.clear()
        unlocked.clear()
        notes.clear()
        xp = 0
        focusEndAt = null
        focusRemainingMs = focusMinutes * 60_000L
        selectedDate = LocalDate.now()
        saveHabits()
        saveCompletions()
        saveFocusLog()
        saveUnlocked()
        saveNotes()
        saveFocusTimer()
        prefs.edit().putInt(K_XP, 0).apply()
    }

    private fun loadTheme(): AppTheme = try {
        AppTheme.valueOf(prefs.getString(K_THEME, AppTheme.GOLD.name) ?: AppTheme.GOLD.name)
    } catch (e: IllegalArgumentException) {
        AppTheme.GOLD
    }

    private fun loadHabits(): List<Habit> {
        val raw = prefs.getString(K_HABITS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = ArrayList<Habit>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val daysArr = o.optJSONArray("days")
                val daysList = ArrayList<Int>()
                if (daysArr != null) {
                    for (j in 0 until daysArr.length()) daysList.add(daysArr.getInt(j))
                } else {
                    daysList.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
                }
                list.add(
                    Habit(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        created = o.optString("created", LocalDate.now().toString()),
                        days = daysList,
                        category = o.optString("category", "General")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadCompletions(): Map<String, Set<Long>> {
        val raw = prefs.getString(K_COMPLETIONS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = LinkedHashMap<String, Set<Long>>()
            for (key in obj.keys()) {
                val arr = obj.getJSONArray(key)
                val ids = HashSet<Long>()
                for (i in 0 until arr.length()) ids.add(arr.getLong(i))
                if (ids.isNotEmpty()) map[key] = ids
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun loadFocusLog(): Map<String, List<Int>> {
        val raw = prefs.getString(K_FOCUS_LOG, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = LinkedHashMap<String, List<Int>>()
            for (key in obj.keys()) {
                val arr = obj.getJSONArray(key)
                val list = ArrayList<Int>()
                for (i in 0 until arr.length()) list.add(arr.getInt(i))
                map[key] = list
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun loadUnlocked(): Map<String, String> {
        val raw = prefs.getString(K_UNLOCKED, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = LinkedHashMap<String, String>()
            for (key in obj.keys()) map[key] = obj.getString(key)
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun loadNotes(): Map<String, String> {
        val raw = prefs.getString(K_NOTES, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = LinkedHashMap<String, String>()
            for (key in obj.keys()) map[key] = obj.getString(key)
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveHabits() {
        val arr = JSONArray()
        for (h in habits) {
            val o = JSONObject()
                .put("id", h.id)
                .put("name", h.name)
                .put("created", h.created)
                .put("days", JSONArray(h.days))
                .put("category", h.category)
            arr.put(o)
        }
        prefs.edit().putString(K_HABITS, arr.toString()).apply()
    }

    private fun saveCompletions() {
        val obj = JSONObject()
        for ((day, ids) in completions) obj.put(day, JSONArray(ids.toList()))
        prefs.edit().putString(K_COMPLETIONS, obj.toString()).apply()
    }

    private fun saveFocusLog() {
        val obj = JSONObject()
        for ((day, list) in focusLog) obj.put(day, JSONArray(list))
        prefs.edit().putString(K_FOCUS_LOG, obj.toString()).apply()
    }

    private fun saveUnlocked() {
        val obj = JSONObject()
        for ((id, date) in unlocked) obj.put(id, date)
        prefs.edit().putString(K_UNLOCKED, obj.toString()).apply()
    }

    private fun saveNotes() {
        val obj = JSONObject()
        for ((day, text) in notes) obj.put(day, text)
        prefs.edit().putString(K_NOTES, obj.toString()).apply()
    }

    private fun saveFocusTimer() {
        prefs.edit()
            .putInt(K_FOCUS_MIN, focusMinutes)
            .putLong(K_FOCUS_END, focusEndAt ?: 0L)
            .apply()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Actividad Principal
// ─────────────────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = StarforgeStore(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        setContent { StarforgeApp(store) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tema
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StarforgeTheme(accent: Color, content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = accent,
        onPrimary = Ink,
        secondary = accent,
        onSecondary = Ink,
        background = Ink,
        onBackground = TextMain,
        surface = Navy,
        onSurface = TextMain,
        surfaceVariant = Slate,
        onSurfaceVariant = TextMuted,
        inverseSurface = Slate,
        inverseOnSurface = TextMain,
        outline = Slate
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

// ─────────────────────────────────────────────────────────────────────────────
// Navegación (4 Pestañas)
// ─────────────────────────────────────────────────────────────────────────────

private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("Inicio", Icons.Filled.Home),
    TabItem("Hábitos", Icons.Filled.CheckCircle),
    TabItem("Enfoque", Icons.Filled.Lock),
    TabItem("Perfil", Icons.Filled.Person)
)

@Composable
fun StarforgeApp(store: StarforgeStore) {
    StarforgeTheme(accent = store.theme.accent) {
        val accent = MaterialTheme.colorScheme.primary
        val snackbar = remember { SnackbarHostState() }
        var tab by remember { mutableStateOf(0) }
        var showAdd by remember { mutableStateOf(false) }

        LaunchedEffect(store.toast) {
            val message = store.toast
            if (message != null) {
                snackbar.showSnackbar(message)
                store.toast = null
            }
        }

        LaunchedEffect(store.focusEndAt) {
            val end = store.focusEndAt
            if (end == null) {
                store.focusRemainingMs = store.focusMinutes * 60_000L
                return@LaunchedEffect
            }
            while (true) {
                val left = end - System.currentTimeMillis()
                if (left <= 0L) {
                    store.finishFocus()
                    break
                }
                store.focusRemainingMs = left
                delay(250L)
            }
        }

        Scaffold(
            containerColor = Ink,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = Navy) {
                    TABS.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = tab == index,
                            onClick = { tab = index },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accent,
                                selectedTextColor = accent,
                                indicatorColor = accent.copy(alpha = 0.2f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (tab == 1) {
                    FloatingActionButton(
                        onClick = { showAdd = true },
                        containerColor = accent,
                        contentColor = Ink
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar hábito")
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (tab) {
                    0 -> HomeScreen(store)
                    1 -> HabitsScreen(store)
                    2 -> FocusScreen(store)
                    else -> ProfileScreen(store)
                }
            }
        }

        if (showAdd) {
            AddHabitDialog(
                onDismiss = { showAdd = false },
                onAdd = { name, days, cat ->
                    store.addHabit(name, days, cat)
                    showAdd = false
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes UI Reutilizables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScreenTitle(text: String) {
    Text(text, color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun AppCard(
    modifier: Modifier = Modifier,
    color: Color = Navy,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .padding(18.dp),
        content = content
    )
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun Ring(
    progress: Float,
    color: Color,
    diameter: Dp,
    stroke: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            val arcSize = Size(size.width - sw, size.height - sw)
            val topLeft = Offset(sw / 2f, sw / 2f)
            drawArc(
                color = Slate.copy(alpha = 0.55f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw)
            )
            val sweep = 360f * progress.coerceIn(0f, 1f)
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

@Composable
private fun BarProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Slate.copy(alpha = 0.55f))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Slate,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = TextMuted,
    cursorColor = MaterialTheme.colorScheme.primary
)

private fun longDate(date: LocalDate): String =
    "${DAYS_FULL[date.dayOfWeek.value - 1]} ${date.dayOfMonth} de ${MONTHS_ES[date.monthValue - 1].lowercase()}"

private val MONTHS_ES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

@Composable
private fun WeekStrip(store: StarforgeStore) {
    val accent = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0..6) {
            val date = monday.plusDays(i.toLong())
            val selected = date == store.selectedDate
            val active = store.doneCount(date) > 0
            val future = date.isAfter(today)
            Column(
                Modifier
                    .weight(1f)
                    .alpha(if (future) 0.55f else 1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) accent else Navy)
                    .clickable { store.selectedDate = date }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    DAYS_SHORT[i],
                    color = if (selected) Ink else TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    "${date.dayOfMonth}",
                    color = if (selected) Ink else TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !active -> Color.Transparent
                                selected -> Ink
                                else -> accent
                            }
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla de Inicio
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeScreen(store: StarforgeStore) {
    val accent = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val date = store.selectedDate
    val dayHabits = store.habitsFor(date)
    val done = dayHabits.count { store.isDone(it.id, date) }
    val progress = if (dayHabits.isEmpty()) 0f else done / dayHabits.size.toFloat()
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Buenos días"
        hour < 20 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val streak = store.currentStreak()
    var noteText by remember(date) { mutableStateOf(store.noteFor(date)) }
    val globalSuccess = store.globalSuccessRate()

    ScreenColumn {
        Column {
            Text(greeting, color = TextMuted, fontSize = 14.sp)
            ScreenTitle(store.userName)
            Text(longDate(today), color = TextMuted, fontSize = 13.sp)
        }

        WeekStrip(store)

        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Ring(progress = progress, color = accent, diameter = 96.dp, stroke = 10.dp) {
                    Text(
                        "${(progress * 100).roundToInt()}%",
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(18.dp))
                Column {
                    Text(
                        if (date == today) "Progreso de hoy" else "Progreso del ${longDate(date)}",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$done de ${dayHabits.size} hábitos",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        when {
                            dayHabits.isEmpty() -> "No hay hábitos este día."
                            done == dayHabits.size -> "¡Día completo! 🔥"
                            else -> "Te faltan ${dayHabits.size - done} para cerrar."
                        },
                        color = accent,
                        fontSize = 13.sp
                    )
                }
            }
        }

        AppCard {
            Text("📊 Resumen de Rendimiento", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Ink.copy(alpha = 0.4f)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥 Racha", color = TextMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("$streak días", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Ink.copy(alpha = 0.4f)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯 Éxito", color = TextMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("$globalSuccess%", color = accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Ink.copy(alpha = 0.4f)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 Logros", color = TextMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${store.unlocked.size}/${ACHIEVEMENTS.size}", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        AppCard {
            Text("📝 Nota o Reflexión", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    store.updateNote(date, it)
                },
                placeholder = { Text("Escribe tus pensamientos del día...", color = TextMuted) },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Navy, accent.copy(alpha = 0.35f))))
                .padding(20.dp)
        ) {
            Column {
                Text("FRASE DEL DÍA", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "“${QUOTES[today.dayOfYear % QUOTES.size]}”",
                    color = TextMain,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Nivel ${store.level}", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${store.xpInLevel}/$XP_PER_LEVEL XP", color = TextMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            BarProgress(store.xpInLevel / XP_PER_LEVEL.toFloat(), accent)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla de Hábitos
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HabitsScreen(store: StarforgeStore) {
    val accent = MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val date = store.selectedDate
    val list = store.habitsFor(date)
    val editable = store.canEdit(date)
    val done = list.count { store.isDone(it.id, date) }
    var toDelete by remember { mutableStateOf<Habit?>(null) }

    ScreenColumn {
        ScreenTitle("Hábitos")
        WeekStrip(store)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(longDate(date), color = TextMuted, fontSize = 14.sp)
            if (date != today) {
                TextButton(onClick = { store.selectedDate = today }) {
                    Text("Ir a hoy", color = accent)
                }
            }
        }

        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Completados hoy", color = TextMain, fontWeight = FontWeight.SemiBold)
                Text("$done/${list.size}", color = accent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            BarProgress(if (list.isEmpty()) 0f else done / list.size.toFloat(), accent)
        }

        if (!editable) {
            Text("No puedes marcar hábitos en días futuros.", color = TextMuted, fontSize = 12.sp)
        }

        if (list.isEmpty()) {
            AppCard {
                Text(
                    "No hay hábitos programados para este día. Toca + para agregar uno.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            for (habit in list) {
                key(habit.id) {
                    HabitRow(
                        habit = habit,
                        done = store.isDone(habit.id, date),
                        enabled = editable,
                        onToggle = { store.toggleHabit(habit, date) },
                        onDelete = { toDelete = habit }
                    )
                }
            }
        }

        Spacer(Modifier.height(72.dp))
    }

    toDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = Navy,
            title = { Text("Eliminar hábito", color = TextMain) },
            text = {
                Text(
                    "¿Eliminar “${habit.name}” y su historial? Tus logros y XP se conservan.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.deleteHabit(habit)
                    toDelete = null
                }) { Text("Eliminar", color = accent) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar", color = TextMuted) }
            }
        )
    }
}

@Composable
private fun HabitRow(
    habit: Habit,
    done: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Navy)
            .clickable(enabled = enabled) { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done) accent else Color.Transparent)
                .border(2.dp, if (done) accent else TextMuted, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                habit.name,
                color = if (done) TextMuted else TextMain,
                fontSize = 16.sp,
                textDecoration = if (done) TextDecoration.LineThrough else null
            )
            Spacer(Modifier.height(2.dp))
            Text(
                habit.category,
                color = accent.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = TextMuted)
        }
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, List<Int>, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Salud") }
    val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5, 6, 7) }
    val accent = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Navy,
        title = { Text("Nuevo hábito con frecuencia", color = TextMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 40) text = it },
                    singleLine = true,
                    placeholder = { Text("Ej. Entrenar fuerte", color = TextMuted) },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors()
                )

                Text("Días de repetición:", color = TextMuted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DAYS_SHORT.forEachIndexed { index, dayLabel ->
                        val dayNum = index + 1
                        val active = dayNum in selectedDays
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (active) accent else Slate.copy(alpha = 0.5f))
                                .clickable {
                                    if (active) {
                                        if (selectedDays.size > 1) selectedDays.remove(dayNum)
                                    } else {
                                        selectedDays.add(dayNum)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dayLabel, color = if (active) Ink else TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(text, selectedDays.toList(), category) }, enabled = text.isNotBlank()) {
                Text("Agregar", color = if (text.isNotBlank()) accent else TextMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextMuted) }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla de Enfoque
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FocusScreen(store: StarforgeStore) {
    val accent = MaterialTheme.colorScheme.primary
    val running = store.focusEndAt != null
    val total = store.focusMinutes * 60_000f
    val progress = if (running) 1f - store.focusRemainingMs / total else 0f
    val secs = (store.focusRemainingMs + 999L) / 1000L
    val today = LocalDate.now()

    ScreenColumn {
        ScreenTitle("Enfoque")
        Text(
            "Elige una duración y bloquea el ruido. Cada minuto enfocado suma $XP_PER_FOCUS_MINUTE XP.",
            color = TextMuted,
            fontSize = 14.sp
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (m in FOCUS_OPTIONS) {
                val selected = store.focusMinutes == m
                Box(
                    Modifier
                        .weight(1f)
                        .alpha(if (running && !selected) 0.4f else 1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) accent else Navy)
                        .clickable(enabled = !running) { store.selectFocusMinutes(m) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$m min",
                        color = if (selected) Ink else TextMain,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Ring(progress = progress, color = accent, diameter = 250.dp, stroke = 14.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "%02d:%02d".format(secs / 60, secs % 60),
                        color = TextMain,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (running) "Enfocado…" else "Listo para empezar",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Button(
            onClick = { if (running) store.cancelFocus() else store.startFocus() },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) Slate else accent,
                contentColor = if (running) TextMain else Ink
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                if (running) "Cancelar sesión" else "Iniciar enfoque",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Hoy", color = TextMuted, fontSize = 12.sp)
                    Text(
                        "${store.sessionsOn(today)} sesiones · ${store.minutesOn(today)} min",
                        color = TextMain,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", color = TextMuted, fontSize = 12.sp)
                    Text(
                        "${store.totalSessions()} sesiones · ${store.totalFocusMinutes()} min",
                        color = TextMain,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla de Perfil
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileScreen(store: StarforgeStore) {
    val accent = MaterialTheme.colorScheme.primary
    var confirmReset by remember { mutableStateOf(false) }

    ScreenColumn {
        ScreenTitle("Perfil")

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(88.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    store.userName.trim().firstOrNull()?.uppercase() ?: "S",
                    color = Ink,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                store.userName.ifBlank { "Forjador" },
                color = TextMain,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Nivel ${store.level} · ${store.xp} XP",
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Progreso al nivel ${store.level + 1}", color = TextMain, fontWeight = FontWeight.SemiBold)
                Text("${store.xpInLevel}/$XP_PER_LEVEL", color = TextMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            BarProgress(store.xpInLevel / XP_PER_LEVEL.toFloat(), accent)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("🔥 Racha: ${store.currentStreak()} d", color = TextMain, fontSize = 13.sp)
                Text("🏅 Mejor: ${store.bestStreak()} d", color = TextMain, fontSize = 13.sp)
                Text("🏆 ${store.unlocked.size}/${ACHIEVEMENTS.size}", color = TextMain, fontSize = 13.sp)
            }
        }

        AppCard {
            Text("Datos personales", color = TextMain, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = store.userName,
                    onValueChange = { store.updateName(it) },
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = store.userAge,
                    onValueChange = { store.updateAge(it) },
                    label = { Text("Edad") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = store.mission,
                onValueChange = { store.updateMission(it) },
                label = { Text("Misión personal") },
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        AppCard {
            Text("Tema de acento", color = TextMain, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (t in AppTheme.values()) {
                    val selected = store.theme == t
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Ink.copy(alpha = 0.6f) else Color.Transparent)
                            .clickable { store.updateTheme(t) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(t.accent)
                                .border(
                                    if (selected) 2.5.dp else 0.dp,
                                    if (selected) TextMain else Color.Transparent,
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            t.label,
                            color = if (selected) TextMain else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { confirmReset = true },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Reiniciar progreso", color = TextMuted)
        }
        Spacer(Modifier.height(8.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = Navy,
            title = { Text("Reiniciar progreso", color = TextMain) },
            text = {
                Text(
                    "Se borrarán hábitos, notas, historial, XP y logros. Esta acción no se puede deshacer.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.resetProgress()
                    confirmReset = false
                }) { Text("Reiniciar", color = accent) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancelar", color = TextMuted) }
            }
        )
    }
}
