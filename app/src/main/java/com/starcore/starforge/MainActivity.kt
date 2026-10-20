package com.starcore.starforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val Ink = Color(0xFF171526)
private val Violet = Color(0xFF6750A4)
private val Lavender = Color(0xFFE8DEF8)
private val SurfaceLight = Color(0xFFF7F2FA)
private val Green = Color(0xFF2E7D32)
private val Red = Color(0xFFB3261E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StarforgeApp() }
    }
}

data class Habit(val id: Long, val title: String, val frequency: String, val done: Boolean = false)

@Composable
fun StarforgeApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("starforge", Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var age by remember { mutableStateOf(prefs.getString("age", "") ?: "") }
    var vision by remember { mutableStateOf(prefs.getString("vision", "") ?: "") }
    var onboarded by remember { mutableStateOf(prefs.getBoolean("onboarded", false)) }
    var tab by remember { mutableIntStateOf(0) }
    var habits by remember { mutableStateOf(loadHabits(prefs)) }
    var showAdd by remember { mutableStateOf(false) }
    var newHabit by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Diario") }
    var customGoals by remember { mutableStateOf("") }
    var themeDark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }

    val scheme = if (themeDark) darkColorScheme(primary = Lavender, secondary = Violet)
                 else lightColorScheme(primary = Violet, secondary = Color(0xFF625B71), background = SurfaceLight, surface = Color.White)
    MaterialTheme(colorScheme = scheme) {
        if (!onboarded) {
            Onboarding(
                name = name, onName = { name = it },
                age = age, onAge = { age = it },
                vision = vision, onVision = { vision = it },
                customGoals = customGoals, onCustomGoals = { customGoals = it },
                onFinish = {
                    prefs.edit().putString("name", name.trim()).putString("age", age)
                        .putString("vision", vision).putBoolean("onboarded", true).apply()
                    if (habits.isEmpty()) {
                        habits = listOf(
                            Habit(System.currentTimeMillis(), "Mover el cuerpo 20 minutos", "Diario"),
                            Habit(System.currentTimeMillis() + 1, "Leer o aprender algo", "Diario"),
                            Habit(System.currentTimeMillis() + 2, "Trabajar en mi objetivo principal", "Diario")
                        )
                        saveHabits(prefs, habits)
                    }
                    onboarded = true
                }
            )
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        val items = listOf("Inicio", "Hábitos", "Bloqueo", "Perfil")
                        val icons = listOf(Icons.Default.Home, Icons.Default.CheckCircle, Icons.Default.Lock, Icons.Default.Person)
                        items.forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = tab == index,
                                onClick = { tab = index },
                                icon = { Icon(icons[index], contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (tab == 1) FloatingActionButton(onClick = { showAdd = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir hábito")
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    when (tab) {
                        0 -> HomeScreen(name, habits)
                        1 -> HabitsScreen(habits, onToggle = { id, done ->
                            habits = habits.map { if (it.id == id) it.copy(done = done) else it }
                            saveHabits(prefs, habits)
                        })
                        2 -> FocusScreen()
                        else -> ProfileScreen(
                            name, age, vision, habits.count { it.done }, themeDark,
                            onTheme = {
                                themeDark = it
                                prefs.edit().putBoolean("dark", it).apply()
                            },
                            onReset = {
                                prefs.edit().clear().apply()
                                name = ""; age = ""; vision = ""; onboarded = false
                                habits = emptyList(); tab = 0
                            }
                        )
                    }
                }
            }
            if (showAdd) {
                AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = { Text("Nuevo hábito o tarea") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = newHabit, onValueChange = { newHabit = it }, label = { Text("¿Qué vas a cumplir?") }, singleLine = true)
                            Text("Frecuencia", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Diario", "Semanal", "Mensual").forEach {
                                    FilterChip(selected = frequency == it, onClick = { frequency = it }, label = { Text(it) })
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newHabit.isNotBlank()) {
                                habits = habits + Habit(System.currentTimeMillis(), newHabit.trim(), frequency)
                                saveHabits(prefs, habits)
                                newHabit = ""; frequency = "Diario"; showAdd = false
                            }
                        }) { Text("Guardar") }
                    },
                    dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancelar") } }
                )
            }
        }
    }
}

@Composable
fun Onboarding(
    name: String, onName: (String) -> Unit,
    age: String, onAge: (String) -> Unit,
    vision: String, onVision: (String) -> Unit,
    customGoals: String, onCustomGoals: (String) -> Unit,
    onFinish: () -> Unit
) {
    var selected by remember { mutableStateOf(setOf("Disciplina", "Mejorar mi físico")) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 38.dp, bottom = 32.dp)
    ) {
        item {
            Box(Modifier.size(64.dp).background(Violet, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("BIENVENIDO A", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Starforge", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Text("Forja tu disciplina. Construye tu mejor versión.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(value = name, onValueChange = onName, label = { Text("¿Cómo te llamas?") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = age, onValueChange = onAge, label = { Text("¿Cuántos años tienes?") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        item {
            Text("¿Qué quieres llegar a ser?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Describe la persona en la que quieres convertirte.")
            OutlinedTextField(value = vision, onValueChange = onVision, label = { Text("Ej. una persona disciplinada y emprendedora") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
        item {
            Text("¿Qué quieres conseguir?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            val goals = listOf("Disciplina", "Mejorar mi físico", "Crear mi negocio", "Salir a correr", "Estudiar más", "Dormir mejor")
            goals.forEach { goal ->
                Row(Modifier.fillMaxWidth().clickable {
                    selected = if (goal in selected) selected - goal else selected + goal
                }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = goal in selected, onCheckedChange = {
                        selected = if (it) selected + goal else selected - goal
                    })
                    Text(goal)
                }
            }
            OutlinedTextField(value = customGoals, onValueChange = onCustomGoals, label = { Text("Añade tu propio objetivo") }, modifier = Modifier.fillMaxWidth())
            Text("Seleccionados: ${selected.joinToString().ifBlank { "ninguno" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(onClick = onFinish, enabled = name.isNotBlank() && age.toIntOrNull() != null, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Forjar mi camino", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
            Text("Tus datos se guardan en este dispositivo.", modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeScreen(name: String, habits: List<Habit>) {
    val completed = habits.count { it.done }
    val percent = if (habits.isEmpty()) 0 else completed * 100 / habits.size
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Text("STARFORGE  /  INICIO", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text("Vamos, ${name.ifBlank { "forjador" }}.", fontSize = 29.sp, fontWeight = FontWeight.Bold)
            Text("La disciplina se construye con lo que haces hoy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("TU PROGRESO DE HOY", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$percent%", color = MaterialTheme.colorScheme.onPrimary, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(8.dp))
                        Text("$completed de ${habits.size} hábitos", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Lavender, trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .2f))
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("No necesitas sentirte preparado. Necesitas cumplir la promesa que te hiciste.", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("— Recordatorio Starforge", modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Tu semana", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item { WeekStrip() }
        item { Text("Un paso hoy vale más que un plan perfecto mañana.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun WeekStrip() {
    val today = LocalDate.now()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (0..6).forEach { offset ->
            val day = today.plusDays((offset - (today.dayOfWeek.value - 1)).toLong())
            val isToday = day == today
            Column(
                Modifier.weight(1f).padding(horizontal = 2.dp).background(if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)).padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).take(2).uppercase(), fontSize = 10.sp)
                Text(day.dayOfMonth.toString(), fontWeight = FontWeight.Bold)
                if (isToday) Icon(Icons.Default.Circle, contentDescription = "Hoy", modifier = Modifier.size(6.dp), tint = MaterialTheme.colorScheme.primary)
                else Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun HabitsScreen(habits: List<Habit>, onToggle: (Long, Boolean) -> Unit) {
    val completed = habits.count { it.done }
    val percent = if (habits.isEmpty()) 0 else completed * 100 / habits.size
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("CONSTANCIA, NO PERFECCIÓN", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Tus hábitos", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Hoy cuenta. Marca cada tarea según la cumplas.")
        }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
                            CircularProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxSize(), strokeWidth = 7.dp)
                            Text("$percent%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("$completed de ${habits.size} completados", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(if (percent == 100 && habits.isNotEmpty()) "Todo cumplido. Mantén el ritmo." else "Sigue avanzando, una tarea a la vez.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { WeekStrip() }
        if (habits.isEmpty()) item { Text("Aún no tienes hábitos. Pulsa + para crear el primero.") }
        items(habits.size) { index ->
            val habit = habits[index]
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = habit.done, onCheckedChange = { onToggle(habit.id, it) })
                    Column(Modifier.weight(1f)) {
                        Text(habit.title, fontWeight = FontWeight.SemiBold)
                        Text(habit.frequency, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(color = if (habit.done) Color(0xFFD7F5D2) else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
                        Text(if (habit.done) "Hecho" else "Pendiente", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = if (habit.done) Green else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FocusScreen() {
    var minutes by remember { mutableIntStateOf(25) }
    var sessionStarted by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("PROTEGE TU ATENCIÓN", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Modo enfoque", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Reduce las distracciones y dedica tiempo a lo que importa.")
        }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(if (sessionStarted) "Sesión en curso" else "Tu próxima sesión", fontWeight = FontWeight.SemiBold)
                    Text("%02d:00".format(minutes), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 45, 60).forEach { m ->
                            FilterChip(selected = minutes == m, onClick = { minutes = m; sessionStarted = false }, label = { Text("${m}m") })
                        }
                    }
                    Button(onClick = { sessionStarted = !sessionStarted }, modifier = Modifier.fillMaxWidth()) {
                        Icon(if (sessionStarted) Icons.Default.Check else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text(if (sessionStarted) "Finalizar sesión" else "Iniciar enfoque")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Bloqueo de aplicaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Esta primera versión incluye sesiones de enfoque. El bloqueo real de otras apps necesita permisos y una implementación nativa adicional.", modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(name: String, age: String, vision: String, completed: Int, dark: Boolean, onTheme: (Boolean) -> Unit, onReset: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("TU CAMINO", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Perfil", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(name.ifBlank { "Forjador" }, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("$age años · Nivel ${1 + completed / 5}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Cada hábito suma experiencia", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Mi visión", fontWeight = FontWeight.Bold)
                    Text(vision.ifBlank { "Tu visión personal aparecerá aquí." }, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Tema oscuro", fontWeight = FontWeight.SemiBold)
                        Text("Personaliza tu experiencia", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = dark, onCheckedChange = onTheme)
                }
            }
        }
        item {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Reiniciar configuración")
            }
        }
    }
}

private fun loadHabits(contextPrefs: android.content.SharedPreferences): List<Habit> {
    return try {
        val array = JSONArray(contextPrefs.getString("habits", "[]"))
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Habit(obj.getLong("id"), obj.getString("title"), obj.getString("frequency"), obj.optBoolean("done", false))
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveHabits(contextPrefs: android.content.SharedPreferences, habits: List<Habit>) {
    val array = JSONArray()
    habits.forEach { habit ->
        array.put(JSONObject().put("id", habit.id).put("title", habit.title).put("frequency", habit.frequency).put("done", habit.done))
    }
    contextPrefs.edit().putString("habits", array.toString()).apply()
}
