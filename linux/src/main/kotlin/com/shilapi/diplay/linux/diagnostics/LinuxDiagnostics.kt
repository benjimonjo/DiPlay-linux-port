package com.shilapi.diplay.linux.diagnostics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/** Read-only host inspection. This class never changes network or device configuration. */
class LinuxDiagnostics {
    suspend fun collect(): DiagnosticSnapshot = withContext(Dispatchers.IO) {
        val interfaces = File("/sys/class/net").listFiles().orEmpty()
            .filter { it.name != "lo" }
            .sortedBy { it.name }
            .map { iface ->
                val type = File(iface, "type").readTextOrNull()?.trim()
                val state = File(iface, "operstate").readTextOrNull()?.trim() ?: "unknown"
                NetworkInterfaceInfo(iface.name, if (type == "1") "network" else "other", state)
            }

        val usb = runCommand("lsusb")
        val wifi = runCommand("iw", "dev")
        val nm = runCommand("nmcli", "-t", "-f", "DEVICE,TYPE,STATE", "device")
        val bluetooth = runCommand("bluetoothctl", "list")
        val rfkill = runCommand("rfkill", "list")

        DiagnosticSnapshot(
            kernel = System.getProperty("os.version") ?: "Unknown",
            architecture = System.getProperty("os.arch") ?: "Unknown",
            interfaces = interfaces,
            tunAvailable = File("/dev/net/tun").exists(),
            usbSummary = usb.output,
            wifiSummary = wifi.output,
            networkManagerSummary = nm.output,
            bluetoothSummary = bluetooth.output,
            rfkillSummary = rfkill.output,
            commandNotes = listOf(usb, wifi, nm, bluetooth, rfkill)
                .filter { it.note != null }
                .map { "${it.command}: ${it.note}" },
        )
    }

    private fun runCommand(vararg args: String): CommandResult {
        val command = args.joinToString(" ")
        return try {
            val process = ProcessBuilder(args.toList())
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(4, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                CommandResult(command, "", "timed out after 4 seconds")
            } else {
                val output = process.inputStream.bufferedReader().use { it.readText().trim() }
                CommandResult(command, output.ifBlank { "(no output)" }, if (process.exitValue() == 0) null else "exit ${process.exitValue()}")
            }
        } catch (e: Exception) {
            CommandResult(command, "Unavailable", e.message ?: e.javaClass.simpleName)
        }
    }

    private fun File.readTextOrNull(): String? = runCatching { readText() }.getOrNull()
}

data class NetworkInterfaceInfo(val name: String, val kind: String, val state: String)

data class DiagnosticSnapshot(
    val kernel: String,
    val architecture: String,
    val interfaces: List<NetworkInterfaceInfo>,
    val tunAvailable: Boolean,
    val usbSummary: String,
    val wifiSummary: String,
    val networkManagerSummary: String,
    val bluetoothSummary: String,
    val rfkillSummary: String,
    val commandNotes: List<String>,
)

private data class CommandResult(val command: String, val output: String, val note: String?)
