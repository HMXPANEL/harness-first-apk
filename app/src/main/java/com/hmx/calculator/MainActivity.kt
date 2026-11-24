package com.hmx.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { CalculatorApp() } }
}

private data class Entry(val expression: String, val result: String)
private val Purple = Color(0xFF9B7BFF)

@Composable
private fun CalculatorApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("calculator", Context.MODE_PRIVATE) }
    var dark by rememberSaveable { mutableStateOf(prefs.getBoolean("dark", true)) }
    var haptics by rememberSaveable { mutableStateOf(prefs.getBoolean("haptics", true)) }
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<Entry>().apply { addAll(prefs.getString("history", "").orEmpty().split("\n").filter { it.contains("|") }.map { it.substringBefore("|") to it.substringAfter("|") }.map { Entry(it.first, it.second) }) } }
    val background = if (dark) Color(0xFF100E17) else Color(0xFFF6F3FC)
    val surface = if (dark) Color(0xFF201D2B) else Color.White
    val foreground = if (dark) Color(0xFFF5F1FF) else Color(0xFF211B2C)
    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Purple, background = background, surface = surface) else lightColorScheme(primary = Color(0xFF6944D4), background = background, surface = surface)) {
        Surface(Modifier.fillMaxSize(), color = background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("HMX", color = Purple, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showHistory = true }) { Icon(Icons.Default.History, "History", tint = foreground) }
                        IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, "Settings", tint = foreground) }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(expression.ifEmpty { "0" }, modifier = Modifier.fillMaxWidth(), color = foreground.copy(alpha = .68f), fontSize = 30.sp, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    if (result.isNotBlank()) IconButton(onClick = { (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Calculator result", result)) }) { Icon(Icons.Default.ContentCopy, "Copy result", tint = Purple) }
                    Text(if (error) "Error" else result.ifEmpty { "" }, color = if (error) Color(0xFFFF6C88) else foreground, fontSize = 48.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                val rows = listOf(listOf("AC", "DEL", "(", ")"), listOf("7", "8", "9", "÷"), listOf("4", "5", "6", "×"), listOf("1", "2", "3", "−"), listOf("%", "0", ".", "+"))
                rows.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { label ->
                            CalcButton(label, Modifier.weight(1f), surface, foreground, label in listOf("AC", "DEL") || label in listOf("(", ")", "%"), feedbackEnabled = haptics) {
                                error = false
                                when (label) {
                                    "AC" -> { expression = ""; result = "" }
                                    "DEL" -> { expression = expression.dropLast(1); result = preview(expression) }
                                    else -> { expression += when (label) { "÷" -> "/"; "×" -> "*"; "−" -> "-"; else -> label }; result = preview(expression) }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CalcButton("±", Modifier.weight(1f), surface, foreground, true, feedbackEnabled = haptics) { expression = if (expression.startsWith("-")) expression.drop(1) else "-($expression)"; result = preview(expression) }
                    CalcButton("0", Modifier.weight(1f), surface, foreground, false, feedbackEnabled = haptics) { expression += "0"; result = preview(expression) }
                    CalcButton("=", Modifier.weight(2f), Purple, Color.White, false, true, feedbackEnabled = haptics) {
                        try { val value = CalculatorEngine.format(CalculatorEngine.evaluate(expression)); result = value; error = false; if (expression.isNotBlank()) { history.add(0, Entry(expression, value)); while (history.size > 100) history.removeAt(history.lastIndex); prefs.edit().putString("history", history.joinToString("\n") { "${it.expression}|${it.result}" }).apply() }; expression = value }
                        catch (_: Exception) { error = true; result = "" }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
        if (showHistory) AlertDialog(onDismissRequest = { showHistory = false }, title = { Text("History") }, text = {
            if (history.isEmpty()) Text("Your calculations will appear here.") else LazyColumn(Modifier.heightIn(max = 360.dp)) { items(history) { item -> Column(Modifier.fillMaxWidth().clickable { expression = item.expression; result = item.result; showHistory = false }.padding(vertical = 10.dp)) { Text(item.expression, color = foreground.copy(alpha = .65f)); Text(item.result, color = foreground, fontSize = 20.sp) }; HorizontalDivider() } }
        }, confirmButton = { TextButton(onClick = { history.clear(); prefs.edit().remove("history").apply() }) { Text("Clear") } }, dismissButton = { TextButton(onClick = { showHistory = false }) { Text("Done") } })
        if (showSettings) AlertDialog(onDismissRequest = { showSettings = false }, title = { Text("Preferences") }, text = { Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Dark theme"); Switch(dark, { dark = it; prefs.edit().putBoolean("dark", it).apply() }) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Haptic feedback"); Switch(haptics, { haptics = it; prefs.edit().putBoolean("haptics", it).apply() }) }
        } }, confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Done") } })
    }
}

private fun preview(expression: String): String = try { if (expression.isBlank()) "" else CalculatorEngine.format(CalculatorEngine.evaluate(expression)) } catch (_: Exception) { "" }

@Composable
private fun CalcButton(label: String, modifier: Modifier, color: Color, foreground: Color, secondary: Boolean, emphasized: Boolean = false, feedbackEnabled: Boolean = true, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (pressed) .94f else 1f, label = "buttonScale")
    val targetColor = if (secondary) color.copy(alpha = .72f) else color
    val animatedColor by animateColorAsState(targetColor, label = "buttonColor")
    Surface(modifier = modifier.height(68.dp).scale(scale), shape = RoundedCornerShape(22.dp), color = animatedColor, tonalElevation = if (emphasized) 5.dp else 0.dp) {
        Box(Modifier.fillMaxSize().clickable { pressed = true; if (feedbackEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick(); pressed = false }, contentAlignment = Alignment.Center) {
            Text(label, color = if (secondary) Purple else foreground, fontSize = if (label.length > 2) 18.sp else 23.sp, fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium)
        }
    }
}
