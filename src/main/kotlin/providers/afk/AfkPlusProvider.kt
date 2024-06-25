package xyz.sexnine.ninesleep.providers.afk

import net.lapismc.afkplus.api.AFKPlusPlayerAPI
import net.lapismc.afkplus.api.AFKStartEvent
import net.lapismc.afkplus.api.AFKStopEvent
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import xyz.sexnine.ninesleep.ExclusionProvider
import xyz.sexnine.ninesleep.P
import xyz.sexnine.ninesleep.WorldCheckCallback

class AfkPlusProvider : ExclusionProvider, Listener {
    private var checkWorldCallback: WorldCheckCallback? = null
    private val afkPlusAPI: AFKPlusPlayerAPI

    init {
        try {
            afkPlusAPI = AFKPlusPlayerAPI()
        } catch (e: NoClassDefFoundError) {
            throw Error("AFKPlus is not enabled, is it installed?")
        }
    }

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
        afkPlusAPI.getPlayer(player.uniqueId).isAFK && !player.isSleeping


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onAfkStart(e: AFKStartEvent) {
        P.messenger.debug("AFKStartEvent triggered")

        P.server.scheduler.runTaskLater(P, Runnable {
            val world = P.server.getPlayer(e.player.uuid)?.world ?: return@Runnable
            checkWorldCallback?.invoke(world)
        }, 1)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onAfkStop(e: AFKStopEvent) {
        P.messenger.debug("AFKStopEvent triggered")

        P.server.scheduler.runTaskLater(P, Runnable {
            val world = P.server.getPlayer(e.player.uuid)?.world ?: return@Runnable
            checkWorldCallback?.invoke(world)
        }, 1)
    }
}