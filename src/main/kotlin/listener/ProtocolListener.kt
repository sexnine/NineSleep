package xyz.sexnine.ninesleep.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import xyz.sexnine.ninesleep.P

fun addProtocolListener(manager: ProtocolManager) {
    P.messenger.debug("Registering protocol listeners")

    manager.addPacketListener(
        object : PacketAdapter(P, ListenerPriority.NORMAL, PacketType.Play.Server.SYSTEM_CHAT) {
            override fun onPacketSending(e: PacketEvent) {
                val isActionBar = e.packet.booleans.read(0)
                if (!isActionBar) {
                    return
                }

                val content = e.packet.chatComponents.read(0).json
                if (
                    content == "{\"translate\":\"sleep.skipping_night\"}" ||
                    content.startsWith("{\"translate\":\"sleep.players_sleeping\",")
                ) {
                    P.messenger.debug("Cancelling vanilla action bar")
                    e.isCancelled = true
                }
            }
        }
    )
}