package xyz.sexnine.ninesleep

import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

const val FULL_DAY_TIME = 24000

sealed class CheckWorldReason {
    data class PlayerEnteredBed(val player: Player) : CheckWorldReason()
    data class PlayerLeftBed(val player: Player) : CheckWorldReason()
    data class PlayerEnteredWorld(val player: Player) : CheckWorldReason()
    data class PlayerLeftWorld(val player: Player) : CheckWorldReason()
    data object ExclusionProviderCheck : CheckWorldReason()
    data object Other : CheckWorldReason()
}

data class CheckValues(val numSleeping: Int, val neededSleeping: Int)

class Controller {
    val exclusionProviders = mutableListOf<ExclusionProvider>()
    private val worldSkipTasks = mutableMapOf<World, BukkitTask>()
    private val previousCheckValues = mutableMapOf<World, CheckValues>()

    fun checkWorld(world: World, reason: CheckWorldReason) {
        if (world.name in P.config.ignoreWorlds) {
            return
        }

        P.messenger.debug { "Checking world ${world.name}" }

        val players = world.players
        val numSleeping = players.count { it.isSleeping }

        val filteredPlayers = filterExcludedPlayers(players, world)

        val percentageRequired = P.config.percentageRequired
        val neededSleeping = when (P.config.roundingMethod) {
            "floor" -> ::floor
            "round" -> ::round
            else -> ::ceil
        }.invoke(percentageRequired * filteredPlayers.size).toInt()
        val percentageSleeping = (numSleeping.toDouble() / neededSleeping).let { if (it.isNaN()) 1.0 else it }

        P.messenger.debug {
            "Players: ${filteredPlayers.count()} (${players.count()}) | Sleeping: $numSleeping ($percentageSleeping) | Needed: $neededSleeping ($percentageRequired)"
        }

        val previousCheckValue by lazy { previousCheckValues[world] }
        val valuesChanged by lazy {
            previousCheckValue ?: return@lazy numSleeping != 0

            if (previousCheckValue!!.numSleeping == 0 && numSleeping == 0) {
                return@lazy false
            }

            previousCheckValue!!.numSleeping != numSleeping || previousCheckValue!!.neededSleeping != neededSleeping
        }

        when (reason) {
            is CheckWorldReason.PlayerEnteredBed -> {
                P.messenger.playerEnteredBed(
                    reason.player,
                    numSleeping,
                    neededSleeping,
                    max(0, neededSleeping - numSleeping)
                )
            }

            is CheckWorldReason.PlayerLeftBed -> {
                if (isProbablyAbleToSleep(world)) {
                    // We can assume that it probably wasn't caused by the night passing, so we'll send a message
                    P.messenger.playerLeftBed(
                        reason.player,
                        numSleeping,
                        neededSleeping,
                        max(0, neededSleeping - numSleeping)
                    )
                }
            }

            else -> {
                if (valuesChanged) {
                    P.messenger.genericSleeping(numSleeping, neededSleeping)
                }
            }
        }

        if (
            numSleeping >= 1 &&
            percentageSleeping >= percentageRequired &&
            world.time < FULL_DAY_TIME - 101
        ) {
            if (world !in worldSkipTasks) {
                P.messenger.debug("All sleeping, skipping night")

                val task = P.server.scheduler.runTaskLater(
                    P,
                    Runnable { skipNight(world) },
                    100
                )
                worldSkipTasks[world] = task
            }
        } else {
            val task = worldSkipTasks.remove(world)
            if (task != null && !task.isCancelled) {
                P.messenger.debug("Cancelling skipping night")
                task.cancel()
            }
            previousCheckValues[world] = CheckValues(numSleeping, neededSleeping)
        }
    }

    private fun isProbablyAbleToSleep(world: World): Boolean =
        (world.hasStorm() && world.isThundering) || world.time > 12010


    private fun filterExcludedPlayers(players: List<Player>, world: World): List<Player> =
        players.filter { player ->
            exclusionProviders.none { it.isExcluded(player, world) }
        }


    private fun skipNight(world: World) {
        world.fullTime += FULL_DAY_TIME - world.time
        world.players.forEach { it.setStatistic(Statistic.TIME_SINCE_REST, 0) }

        if (!world.isClearWeather) {
            P.messenger.debug("Clearing weather")
            world.setStorm(false)
        }

        previousCheckValues.remove(world)
        worldSkipTasks.remove(world)
        P.messenger.debug("Skipping night complete")
    }
}