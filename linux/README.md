# DiPlay for Linux (initial desktop shell)

This is a separate Kotlin/JVM desktop target. It does not replace or modify the Android application modules.

## Current scope

- Compose Multiplatform desktop window.
- JVM system-tray integration with Open and Quit actions.
- Selection UI for USB, existing Wi-Fi, Bluetooth + Wi-Fi, Wi-Fi Direct and hotspot modes.
- Read-only host diagnostics for kernel, network interfaces, TUN, USB, Wi-Fi, NetworkManager, Bluetooth and rfkill.
- No privileged commands and no changes to NetworkManager, wpa_supplicant, BlueZ, routing or USB device configuration.

The connection controls are intentionally not wired to transport backends yet. The UI labels this explicitly rather than implying that a CarPlay session can already be established.

## Build and run

Requirements: JDK 21 and a working Gradle 9.5 wrapper download/cache.

```bash
./gradlew :linux:run
```

Build a Linux package:

```bash
./gradlew :linux:packageDistributionForCurrentOS
```

The current tray implementation uses `java.awt.SystemTray`. It provides a useful initial tray path on Linux desktops that expose the AWT tray bridge. A native KDE StatusNotifierItem implementation is a follow-up task; on Wayland sessions where the AWT tray bridge is unavailable, the window remains usable and the header reports that the tray is unavailable.

## Planned backend boundaries

- `transport`: connection-mode model and future transport contracts.
- `diagnostics`: read-only host inspection.
- `tray`: desktop tray lifecycle.
- `app`: Compose desktop presentation.

Future platform adapters should be isolated behind interfaces. Do not run the entire application as root; use udev permissions for USB and a narrowly scoped privileged helper only if networking operations later require it.
