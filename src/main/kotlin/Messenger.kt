package xyz.sexnine.ninesleep

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.util.logging.Level

class Messenger {
    private val minimessage = MiniMessage.miniMessage()

    fun playerEnteredBed(player: Player, numSleeping: Int, neededSleeping: Int, moreNeeded: Int) {
        val template = P.config.getMessage("chat.player-entered-bed") ?: return

        val message = processTemplate(
            template, mapOf(
                "player" to player.name,
                "num_sleeping" to numSleeping.toString(),
                "needed_sleeping" to neededSleeping.toString(),
                "more_needed" to moreNeeded.toString()
            )
        )

        sendMessage(message)
    }

    fun playerLeftBed(player: Player, numSleeping: Int, neededSleeping: Int, moreNeeded: Int) {
        val template = P.config.getMessage("chat.player-left-bed") ?: return

        val message = processTemplate(
            template, mapOf(
                "player" to player.name,
                "num_sleeping" to numSleeping.toString(),
                "needed_sleeping" to neededSleeping.toString(),
                "more_needed" to moreNeeded.toString()
            )
        )

        sendMessage(message)
    }

    fun genericSleeping(numSleeping: Int, neededSleeping: Int) {
        val template = P.config.getMessage("chat.generic-sleeping") ?: return

        val message = processTemplate(
            template, mapOf(
                "num_sleeping" to numSleeping.toString(),
                "needed_sleeping" to neededSleeping.toString()
            )
        )

        sendMessage(message)
    }

    fun debug(message: String, broadcast: Boolean = true) {
        if (P.config.debug.enabled) {
            debugSend(message, broadcast)
        }
    }

    fun debug(broadcast: Boolean = true, message: () -> String) {
        if (P.config.debug.enabled) {
            debugSend(message(), broadcast)
        }
    }

    private fun debugSend(message: String, broadcast: Boolean) {
        P.logger.log(Level.INFO, "DEBUG: $message")
        if (broadcast && P.config.debug.broadcast) {
            P.server.broadcast(
                Component.text("NS DEBUG: ").color(NamedTextColor.GRAY)
                    .append(Component.text(message).color(NamedTextColor.RED))
            )
        }
    }

    private fun processTemplate(template: String, variables: Map<String, String>): Component {
        var ret = template

        for ((key, value) in variables) {
            ret = ret.replace("%$key%", value)
        }

        return minimessage.deserialize(ret)
    }

    private fun sendMessage(message: Component) {
        P.server.broadcast(message)
    }

    private fun sendMessage(player: Player, message: Component) {
        player.sendMessage(message)
    }

}