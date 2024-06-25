package xyz.sexnine.ninesleep.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.TimeSkipEvent
import xyz.sexnine.ninesleep.CheckWorldReason
import xyz.sexnine.ninesleep.P

class EventListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onBedEnter(e: PlayerBedEnterEvent) {
        if (e.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK) {
            return
        }

        P.server.scheduler.runTaskLater(P, Runnable {
            P.controller.checkWorld(e.player.world, CheckWorldReason.PlayerEnteredBed(e.player))
        }, 1)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onBedLeave(e: PlayerBedLeaveEvent) {
        P.server.scheduler.runTaskLater(P, Runnable {
            P.controller.checkWorld(e.player.world, CheckWorldReason.PlayerLeftBed(e.player))
        }, 1)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        P.controller.checkWorld(e.from, CheckWorldReason.PlayerLeftWorld(e.player))
        P.controller.checkWorld(e.player.world, CheckWorldReason.PlayerEnteredWorld(e.player))
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        P.controller.checkWorld(e.player.world, CheckWorldReason.Other)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        P.server.scheduler.runTaskLater(P, Runnable {
            P.controller.checkWorld(e.player.world, CheckWorldReason.Other)
        }, 1)
    }

    @EventHandler
    fun onTimeSkip(e: TimeSkipEvent) {
        if (e.skipReason == TimeSkipEvent.SkipReason.NIGHT_SKIP && e.world.name !in P.config.ignoreWorlds) {
            P.messenger.debug("Cancelling vanilla time skip event")
            e.isCancelled = true
        }
    }
}