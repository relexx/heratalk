// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ChannelInfoTest {
    private val id = ChannelId("general")
    private val name = DisplayName("General")

    @Test
    fun `constructor throws when memberCount is zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            ChannelInfo(id, name, memberCount = 0)
        }
    }

    @Test
    fun `constructor throws when memberCount is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            ChannelInfo(id, name, memberCount = -1)
        }
    }

    @Test
    fun `constructor throws when memberCount exceeds MAX_MEMBERS`() {
        assertThrows(IllegalArgumentException::class.java) {
            ChannelInfo(id, name, memberCount = ChannelInfo.MAX_MEMBERS + 1)
        }
    }

    @Test
    fun `constructor accepts memberCount of 1 (local peer only)`() {
        val info = ChannelInfo(id, name, memberCount = 1)
        assertEquals(1, info.memberCount)
    }

    @Test
    fun `constructor accepts memberCount equal to MAX_MEMBERS`() {
        val info = ChannelInfo(id, name, memberCount = ChannelInfo.MAX_MEMBERS)
        assertEquals(ChannelInfo.MAX_MEMBERS, info.memberCount)
    }
}
