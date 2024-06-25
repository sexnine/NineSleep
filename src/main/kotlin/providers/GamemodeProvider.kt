package xyz.sexnine.ninesleep.providers

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.plugin.Plugin
import xyz.sexnine.ninesleep.CheckWorldReason
import xyz.sexnine.ninesleep.ExclusionProvider
import xyz.sexnine.ninesleep.P
import xyz.sexnine.ninesleep.WorldCheckCallback

class GamemodeProvider : ExclusionProvider, Listener {
    private var checkWorldCallback: WorldCheckCallback? = null
    private val excludedGameModes = P.config.exclusions.gameMode.excludedGameModes

    override fun setWorldCheckCallback(callback: WorldCheckCallback) {
        checkWorldCallback = callback
    }

    override fun enable(plugin: Plugin) {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun disable(plugin: Plugin) {
        HandlerList.unregisterAll(this)
    }

    override fun isExcluded(player: Player, world: World): Boolean =
        player.gameMode in excludedGameModes


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerGamemodeChange(e: PlayerGameModeChangeEvent) {
        P.server.scheduler.runTaskLater(P, Runnable {
            checkWorldCallback?.invoke(e.player.world)
        }, 1)
    }
}