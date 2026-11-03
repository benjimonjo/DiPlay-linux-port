package com.shilapi.diplay.linux

import com.shilapi.diplay.linux.transport.ConnectionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionModeTest {
    @Test
    fun exposesAllRequiredConnectionModes() {
        assertEquals(
            setOf("USB", "EXISTING_WIFI", "BLUETOOTH_WIFI", "WIFI_DIRECT", "HOTSPOT"),
            ConnectionMode.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun everyModeHasUserFacingCopy() {
        ConnectionMode.entries.forEach { mode ->
            assertTrue(mode.title.isNotBlank())
            assertTrue(mode.description.isNotBlank())
        }
    }
}
