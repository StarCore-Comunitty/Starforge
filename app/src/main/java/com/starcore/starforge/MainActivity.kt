package com.starcore.starforge

/*
 * Starforge — disciplina, hábitos y superación personal.
 *
 * Una sola actividad · Jetpack Compose + Material 3 · SharedPreferences.
 *
 * Pestañas: Inicio · Hábitos · Enfoque · Progreso (Estadísticas + Logros) · Perfil
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
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
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
// Paleta y temas de acento
// ─────────────────────────────────────────────────────────────────────────────

private val Ink = Color(0xFF0F1115)
private val Navy = Color(0xFF1A2B4C)
private val Slate = Color(0xFF3C3F46)
private val Gold = Color(0xFFF4C430)
private val Sapphire = Color(0xFF4C8DFF)
private val Emerald = Color(0xFF2ED8A3)
private val TextMain = Color(0xFFF1F3F7)
private val TextMuted = Color(0xFFA3AAB8)

enum class AppTheme(val label: String, val accent: Color) {
    GOLD("Oro", Gold),
    SAPPHIRE("Zafiro", Sapphire),
    EMERALD("Esmeralda", Emerald)
}

// ─────────────────────────────────────────────────────────────────────────────
// Constantes
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

private const val XP_PER_HABIT = 10
private const val XP_PER_FOCUS_MINUTE = 2
private const val XP_PER_LEVEL = 100
private const val DEFAULT_MISSION = "Forjar disciplina cada día y convertirme en mi mejor versión."

private val FOCUS_OPTIONS = listOf(15, 25, 45, 60)
private val DAYS_SHORT = listOf("L", "M", "X", "J", "V", "S", "D")
private val DAYS_FULL = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
private val MONTHS_ES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
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

data class Habit(val id: Long, val name: String, val created: String)

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

data class MonthStats(
    val month: YearMonth,
    val dayRates: List<Float?>,
    val completed: Int,
    val scheduled: Int,
    val successRate: Int,
    val activeDays: Int,
    val elapsedDays: Int,
    val consistency: Int,
    val perfectDays: Int,
    val bestRun: Int,
    val focusSessions: Int,
    val focusMinutes: Int,
    val bestWeekday: Int
)

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
// Estado + persistencia (SharedPreferences)
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

    init {
        if (prefs.contains(K_HABITS)) {
            habits.addAll(loadHabits())
        } else {
            val today = LocalDate.now().toString()
            habits.add(Habit(1L, "Beber 2 litros de agua", today))
            habits.add(Habit(2L, "Leer 10 páginas", today))
            habits.add(Habit(3L, "Ejercicio 20 minutos", today))
            saveHabits()
        }
        completions.putAll(loadCompletions())
        focusLog.putAll(loadFocusLog())
        unlocked.putAll(loadUnlocked())
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

    fun habitsFor(date: LocalDate): List<Habit> {
        val key = date.toString()
        return habits.filter { it.created <= key }
    }

    fun isDone(habitId: Long, date: LocalDate): Boolean =
        completions[date.toString()]?.contains(habitId) == true

    fun doneCount(date: LocalDate): Int = completions[date.toString()]?.size ?: 0

    fun canEdit(date: LocalDate): Boolean = !date.isAfter(LocalDate.now())

    fun addHabit(name: String) {
        val clean = name.trim().take(40)
        if (clean.isEmpty()) return
        var id = System.currentTimeMillis()
        while (habits.any { it.id == id }) id++
        habits.add(Habit(id, clean, LocalDate.now().toString()))
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

    fun monthStats(month: YearMonth): MonthStats {
        val today = LocalDate.now()
        val current = YearMonth.from(today)
        val lastDay = when {
            month.isAfter(current) -> 0
            month == current -> today.dayOfMonth
            else -> month.lengthOfMonth()
        }

        val rates = ArrayList<Float?>()
        var scheduledTotal = 0
        var doneTotal = 0
        var activeDays = 0
        var perfectDays = 0
        var bestRun = 0
        var run = 0
        val weekdayDone = IntArray(7)

        for (d in 1..month.lengthOfMonth()) {
            if (d > lastDay) {
                rates.add(null)
                continue
            }
            val date = month.atDay(d)
            val scheduled = habitsFor(date).size
            val done = minOf(doneCount(date), scheduled)
            scheduledTotal += scheduled
            doneTotal += done
            rates.add(if (scheduled > 0) done / scheduled.toFloat() else null)
            if (done > 0) {
                activeDays++
                run++
                if (run > bestRun) bestRun = run
                weekdayDone[date.dayOfWeek.value - 1] += done
            } else {
                run = 0
            }
            if (scheduled > 0 && done >= scheduled) perfectDays++
        }

        var sessions = 0
        var minutes = 0
        val prefix = month.toString()
        for ((key, list) in focusLog) {
            if (key.startsWith(prefix)) {
                sessions += list.size
                minutes += list.sum()
            }
        }

        val maxDone = weekdayDone.maxOrNull() ?: 0
        val bestWeekday = if (maxDone > 0) weekdayDone.indexOf(maxDone) else -1

        return MonthStats(
            month = month,
            dayRates = rates,
            completed = doneTotal,
            scheduled = scheduledTotal,
            successRate = if (scheduledTotal > 0) (doneTotal * 100f / scheduledTotal).roundToInt() else 0,
            activeDays = activeDays,
            elapsedDays = lastDay,
            consistency = if (lastDay > 0) (activeDays * 100f / lastDay).roundToInt() else 0,
            perfectDays = perfectDays,
            bestRun = bestRun,
            focusSessions = sessions,
            focusMinutes = minutes,
            bestWeekday = bestWeekday
        )
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

    fun resetProgress() {
        habits.clear()
        completions.clear()
        focusLog.clear()
        unlocked.clear()
        xp = 0
        focusEndAt = null
        focusRemainingMs = focusMinutes * 60_000L
        selectedDate = LocalDate.now()
        saveHabits()
        saveCompletions()
        saveFocusLog()
        saveUnlocked()
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
                list.add(
                    Habit(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        created = o.optString("created", LocalDate.now().toString())
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

    private fun saveHabits() {
        val arr = JSONArray()
        for (h in habits) {
            arr.put(JSONObject().put("id", h.id).put("name", h.name).put("created", h.created))
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

    private fun saveFocusTimer() {
        prefs.edit()
            .putInt(K_FOCUS_MIN, focusMinutes)
            .putLong(K_FOCUS_END, focusEndAt ?: 0L)
            .apply()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Actividad
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
// Raíz de la app
// ─────────────────────────────────────────────────────────────────────────────

private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("Inicio", Icons.Filled.Home),
    TabItem("Hábitos", Icons.Filled.CheckCircle),
    TabItem("Enfoque", Icons.Filled.Lock),
    TabItem("Progreso", Icons.Filled.Star),
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
                NavigationBar(container
