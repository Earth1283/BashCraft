package io.github.Earth1283.bashCraft

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object SessionManager : Listener {
    internal val sessions = mutableMapOf<String, TerminalSession>()

    fun getOrCreate(sender: CommandSender): TerminalSession {
        val key = keyFor(sender)
        return sessions.getOrPut(key) {
            val session = TerminalSession(key)
            if (sender is Player) {
                session.variables["USER"] = sender.name
                session.variables["HOME"] = "/${sender.world.name}"
                session.workingPath = "/${sender.world.name}"
            }
            session
        }
    }

    fun remove(key: String) = sessions.remove(key)

    fun keyFor(sender: CommandSender): String = when (sender) {
        is Player -> sender.uniqueId.toString()
        is ConsoleCommandSender -> "CONSOLE"
        else -> "RCON_${sender.name}"
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        getOrCreate(e.player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        remove(e.player.uniqueId.toString())
    }
}
