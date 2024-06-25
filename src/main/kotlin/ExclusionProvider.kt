package xyz.sexnine.ninesleep

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

typealias WorldCheckCallback = (world: World) -> Unit

/**
 * This is guaranteed to be initialized after the plugin is enabled
 */
interface ExclusionProvider {
    fun setWorldCheckCallback(callback: WorldCheckCallback)

    fun enable(plugin: Plugin)

    fun disable(plugin: Plugin)

    fun isExcluded(player: Player, world: World): Boolean
}