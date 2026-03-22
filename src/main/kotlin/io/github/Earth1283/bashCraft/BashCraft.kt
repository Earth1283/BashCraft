package io.github.Earth1283.bashCraft

import org.bukkit.plugin.java.JavaPlugin

class BashCraft : JavaPlugin() {

    companion object {
        var startTime: Long = 0
        var instance: BashCraft? = null
    }

    override fun onEnable() {
        startTime = System.currentTimeMillis()
        instance = this

        // Register player session listener
        server.pluginManager.registerEvents(SessionManager, this)

        // Register all commands via CommandMap reflection
        CommandRegistry.register(this)

        logger.info("BashCraft enabled — sudo make me a sandwich")
    }

    override fun onDisable() {
        instance = null
        logger.info("BashCraft disabled")
    }
}
