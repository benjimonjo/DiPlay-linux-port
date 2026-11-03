package com.shilapi.diplay.linux.tray

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage

/** Desktop tray fallback implemented through the JVM AWT system tray. */
class LinuxTray(
    private val onShow: () -> Unit,
    private val onExit: () -> Unit,
) : AutoCloseable {
    private var trayIcon: TrayIcon? = null

    fun install(): Boolean {
        if (!SystemTray.isSupported()) return false
        return runCatching {
            val menu = PopupMenu().apply {
                add(MenuItem("Open DiPlay").apply { addActionListener { onShow() } })
                addSeparator()
                add(MenuItem("Quit").apply { addActionListener { onExit() } })
            }
            trayIcon = TrayIcon(createIcon(), "DiPlay — CarPlay receiver", menu).apply {
                isImageAutoSize = true
                addActionListener { onShow() }
            }
            SystemTray.getSystemTray().add(trayIcon)
            true
        }.getOrDefault(false)
    }

    override fun close() {
        trayIcon?.let { runCatching { SystemTray.getSystemTray().remove(it) } }
        trayIcon = null
    }

    private fun createIcon(): BufferedImage = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB).also { image ->
        val g = image.createGraphics()
        try {
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            g.color = Color(35, 115, 210)
            g.fillRoundRect(2, 2, 60, 60, 16, 16)
            g.color = Color.WHITE
            g.font = Font(Font.SANS_SERIF, Font.BOLD, 38)
            val metrics = g.fontMetrics
            val label = "D"
            g.drawString(label, (64 - metrics.stringWidth(label)) / 2, 45)
        } finally {
            g.dispose()
        }
    }
}
