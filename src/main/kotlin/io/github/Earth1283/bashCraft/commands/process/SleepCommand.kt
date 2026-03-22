package io.github.Earth1283.bashCraft.commands.process

import io.github.Earth1283.bashCraft.BashCraft
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class SleepCommand : LinuxCommand("sleep", "Delay for a specified time", "sleep <seconds>") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val secs = args.firstOrNull()?.toDoubleOrNull()
            ?: return listOf("sleep: missing or invalid time operand")

        val ticks = (secs * 20).toLong().coerceIn(1, 600) // max 30 seconds
        val plugin = BashCraft.instance ?: return listOf("sleep: plugin not initialized")

        sender.sendMessage(Term.dim("ZZZ... sleeping for ${secs}s"))
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            sender.sendMessage(Term.ok("... woke up after ${secs}s"))
            if (session.promptMode) sender.sendMessage(Term.prompt(sender.name, session.workingPath))
        }, ticks)

        return emptyList()
    }
}
