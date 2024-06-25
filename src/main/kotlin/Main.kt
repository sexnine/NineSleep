package xyz.sexnine.ninesleep

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.sexnine.ninesleep.providers.afk.AfkPlusProvider
import xyz.sexnine.ninesleep.listener.EventListener
import xyz.sexnine.ninesleep.listener.addProtocolListener
import xyz.sexnine.ninesleep.providers.GamemodeProvider

val P get() = NineSleep.instance

class NineSleep : JavaPlugin() {
    companion object {
        private var internalInstance: NineSleep? = null

        @JvmStatic
        val instance: NineSleep
            get() = internalInstance
                ?: throw IllegalStateException("Could not get instance; is the plugin initialized?")
    }

    init {
        internalInstance = this
    }

    val fileConfig: FileConfiguration get() = getConfig()
    val config = Config()
    val messenger = Messenger()
    val controller = Controller()
    private var protocolManager: ProtocolManager? = null

    override fun onEnable() {
        saveDefaultConfig()

        messenger.debug("Debug mode is enabled", false)

        try {
            protocolManager = ProtocolLibrary.getProtocolManager()
        } catch (e: NoClassDefFoundError) {
            if (config.disableVanillaActionBar) {
                logger.warning("ProtocolLib is not enabled.  Vanilla action bars will not be disabled")
                logger.warning("Please install ProtocolLib from https://github.com/dmulloy2/ProtocolLib/releases")
            }
        }

        if (protocolManager != null && config.disableVanillaActionBar) {
            addProtocolListener(protocolManager!!)
        }

        server.pluginManager.apply {
            registerEvents(EventListener(), this@NineSleep)
        }

        addExclusionProviders()
    }

    private fun addExclusionProviders() {
        if (config.exclusions.gameMode.enabled) {
            addExclusionProvider(GamemodeProvider(), "Gamemode")
        }

        if (config.exclusions.afk.enabled) {
            try {
                val afkProvider: ExclusionProvider = when (config.exclusions.afk.method) {
                    "afk-plus" -> AfkPlusProvider()
                    else -> {
                        throw Error("AFK method is not supported")
                    }
                }
                addExclusionProvider(afkProvider, "AFK")
            } catch (e: Error) {
                logger.severe("Error initializing AFK exclusion rule: ${e.message ?: "Unknown error"}")
                logger.warning("AFK exclusion rule will be ignored due to error")
            }
        }
    }

    private fun addExclusionProvider(provider: ExclusionProvider, name: String) {
        provider.setWorldCheckCallback {
            controller.checkWorld(it, CheckWorldReason.ExclusionProviderCheck)
        }
        provider.enable(this)
        controller.exclusionProviders.add(provider)

        logger.info("$name exclusion rule enabled")
    }
}