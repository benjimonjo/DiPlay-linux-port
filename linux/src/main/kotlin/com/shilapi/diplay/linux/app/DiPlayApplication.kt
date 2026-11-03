package com.shilapi.diplay.linux.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.SelectionContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.shilapi.diplay.linux.diagnostics.DiagnosticSnapshot
import com.shilapi.diplay.linux.diagnostics.LinuxDiagnostics
import com.shilapi.diplay.linux.transport.ConnectionMode
import com.shilapi.diplay.linux.tray.LinuxTray
import kotlinx.coroutines.launch

private val Accent = Color(0xFF62A8FF)

@Composable
fun DiPlayApplication(onExit: () -> Unit) {
    var windowVisible by remember { mutableStateOf(true) }
    val tray = remember {
        LinuxTray(
            onShow = { windowVisible = true },
            onExit = onExit,
        )
    }
    var trayAvailable by remember { mutableStateOf<Boolean?>(null) }

    DisposableEffect(tray) {
        trayAvailable = tray.install()
        onDispose { tray.close() }
    }

    if (windowVisible) {
        Window(
            onCloseRequest = { windowVisible = false },
            title = "DiPlay — Linux CarPlay receiver",
            state = rememberWindowState(width = 1000.dp, height = 760.dp),
        ) {
            DiPlayWindow(
                trayAvailable = trayAvailable == true,
                onQuit = onExit,
            )
        }
    }
}

@Composable
private fun DiPlayWindow(trayAvailable: Boolean, onQuit: () -> Unit) {
    var selectedMode by remember { mutableStateOf(ConnectionMode.USB) }
    var diagnostics by remember { mutableStateOf<DiagnosticSnapshot?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val collector = remember { LinuxDiagnostics() }

    fun refresh() {
        scope.launch {
            loading = true
            diagnostics = collector.collect()
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Accent,
        secondary = Color(0xFF8BC5FF),
        background = Color(0xFF10151D),
        surface = Color(0xFF19222E),
        surfaceVariant = Color(0xFF222E3C),
    )) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Header(trayAvailable)
                ConnectionPanel(selectedMode, onSelect = { selectedMode = it })
                SessionPanel(selectedMode)
                DiagnosticsPanel(diagnostics, loading, onRefresh = ::refresh)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onQuit) { Text("Quit DiPlay") }
                }
            }
        }
    }
}

@Composable
private fun Header(trayAvailable: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.width(58.dp).height(58.dp)
                .background(Accent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("D", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF07111F))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("DiPlay", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Native Linux CarPlay receiver", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("LINUX ALPHA", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(if (trayAvailable) "Tray active" else "Tray unavailable", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ConnectionPanel(selected: ConnectionMode, onSelect: (ConnectionMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading("Connection", "Choose how your iPhone will connect")
        ConnectionMode.entries.forEach { mode ->
            val active = selected == mode
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(mode) },
                border = if (active) BorderStroke(1.dp, Accent) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (active) Color(0xFF1D3046) else MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(mode.title, fontWeight = FontWeight.SemiBold)
                        Text(mode.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(if (active) "SELECTED" else "SELECT", color = if (active) Accent else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SessionPanel(mode: ConnectionMode) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading("CarPlay session", "The transport engine is not connected yet")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(9.dp).height(9.dp).background(Color(0xFFFFC266), RoundedCornerShape(50)))
                Spacer(Modifier.width(9.dp))
                Text("Disconnected", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("${mode.title} · planned", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Text(
                "This first milestone provides the desktop shell and read-only diagnostics. It does not yet attempt pairing, USB configuration changes, Wi-Fi changes or a CarPlay connection.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Connection backend not implemented")
            }
        }
    }
}

@Composable
private fun DiagnosticsPanel(snapshot: DiagnosticSnapshot?, loading: Boolean, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { SectionHeading("Hardware diagnostics", "Read-only host information") }
            OutlinedButton(onClick = onRefresh, enabled = !loading) {
                androidx.compose.material3.Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (loading) "Scanning" else "Refresh")
            }
        }
        if (snapshot == null) {
            Card { Text("Collecting system information…", Modifier.padding(18.dp)) }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    DiagnosticLine("Kernel", "${snapshot.kernel} (${snapshot.architecture})")
                    DiagnosticLine("TUN device", if (snapshot.tunAvailable) "Available" else "Missing")
                    DiagnosticLine("Network interfaces", snapshot.interfaces.joinToString { "${it.name}: ${it.state}" }.ifBlank { "None found" })
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    DiagnosticBlock("USB devices", snapshot.usbSummary)
                    DiagnosticBlock("Wi-Fi interfaces", snapshot.wifiSummary)
                    DiagnosticBlock("NetworkManager", snapshot.networkManagerSummary)
                    DiagnosticBlock("Bluetooth", snapshot.bluetoothSummary)
                    DiagnosticBlock("Radio blocks", snapshot.rfkillSummary)
                    if (snapshot.commandNotes.isNotEmpty()) {
                        Text("Notes: ${snapshot.commandNotes.joinToString(" · ")}", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DiagnosticLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(135.dp))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DiagnosticBlock(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        SelectionContainer {
            Text(content.ifBlank { "No information returned" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}
