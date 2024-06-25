package xyz.sexnine.ninesleep

import org.bukkit.GameMode
import org.bukkit.configuration.file.FileConfiguration

class Config {
    private val config get() = P.fileConfig

    val percentageRequired by lazy { config.getDouble("conditions.percentage") }
    val roundingMethod by lazy { config.getString("conditions.rounding-method", "ceil")!! }
    val exclusions: Exclusions by lazy { Exclusions(config) }
    val ignoreWorlds by lazy { config.getList("ignore-worlds", mutableListOf<String>()) as List<String> }
    val disableVanillaActionBar by lazy { config.getBoolean("disable-vanilla-action-bar") }
    val debug by lazy { DebugConfig(config) }

    fun getMessage(path: String): String? {
        return config.getString("messages.$path")
    }
}

class Exclusions(private val configFile: FileConfiguration) {
    val gameMode by lazy { GamemodeExclusionConfig(configFile) }
    val afk by lazy { AfkExclusionConfig(configFile) }
}

class DebugConfig(private val configFile: FileConfiguration) {
    val enabled by lazy { configFile.getBoolean("debug.enabled") }
    val broadcast by lazy { configFile.getBoolean("debug.broadcast") }
}

class AfkExclusionConfig(private val configFile: FileConfiguration) {
    val enabled by lazy { configFile.getBoolean("exclusions.afk.enabled") }
    val method by lazy { configFile.getString("exclusions.afk.method") }
}

class GamemodeExclusionConfig(private val configFile: FileConfiguration) {
    val enabled by lazy { configFile.getBoolean("exclusions.gamemode.enabled") }
    val excludedGameModes: Set<GameMode> by lazy {
        val gamemodes = mutableSetOf<GameMode>()

        val keysToGameModes = mapOf(
            "survival" to GameMode.SURVIVAL,
            "creative" to GameMode.CREATIVE,
            "adventure" to GameMode.ADVENTURE,
            "spectator" to GameMode.SPECTATOR
        )

        for ((key, gameMode) in keysToGameModes) {
            if (configFile.getBoolean("exclusions.gamemode.gamemodes.$key")) {
                gamemodes.add(gameMode)
            }
        }

        gamemodes
    }
}

