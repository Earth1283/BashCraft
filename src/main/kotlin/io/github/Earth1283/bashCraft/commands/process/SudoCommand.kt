package io.github.Earth1283.bashCraft.commands.process

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SudoCommand : LinuxCommand("sudo", "Execute a command as root (console)", "sudo <command...>") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.sudo")) {
            return listOf(
                "",
                "We trust you have received the usual lecture from the local System",
                "Administrator. It usually boils down to these three things:",
                "",
                "    #1) Respect the privacy of others.",
                "    #2) Think before you type.",
                "    #3) With great power comes great responsibility.",
                "",
                "${sender.name} is not in the sudoers file. This incident will be reported.",
            )
        }

        if (args.isEmpty()) return listOf("sudo: a command is required")

        val cmdLine = args.joinToString(" ").removePrefix("/")
        Bukkit.getServer().logger.warning("[BashCraft] ${sender.name} sudo: /$cmdLine")
        return try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdLine)
            listOf("[sudo] executed: /$cmdLine")
        } catch (e: Exception) {
            listOf("sudo: ${e.message}")
        }
    }
}
