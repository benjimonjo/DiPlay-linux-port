package com.shilapi.diplay.linux.transport

/** Connection choices exposed by the desktop UI. Backends are implemented independently. */
enum class ConnectionMode(
    val title: String,
    val description: String,
) {
    USB("Wired USB", "Connect the iPhone directly over USB."),
    EXISTING_WIFI("Existing Wi-Fi", "Use the Wi-Fi network already connected to this PC."),
    BLUETOOTH_WIFI("Bluetooth + Wi-Fi", "Use Bluetooth for discovery and Wi-Fi for the data session."),
    WIFI_DIRECT("Wi-Fi Direct", "Negotiate a direct peer-to-peer Wi-Fi connection."),
    HOTSPOT("Wi-Fi hotspot", "Host a Wi-Fi network for the iPhone to join."),
}

/** Deliberately reports implementation state separately from hardware presence. */
enum class BackendStage(val label: String) {
    PLANNED("Not implemented yet"),
    DETECTED("Hardware detected"),
    READY("Ready for connection"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    ERROR("Needs attention"),
}
