package io.github.Earth1283.bashCraft.commands.user

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class WhoCommand : LinuxCommand("who", "Show who is logged in", "who", listOf("users")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val players = Bukkit.getOnlinePlayers()
        if (players.isEmpty()) return listOf("(no players online)")
        return players.map { p ->
            val world = p.world.name
            val loc = "${p.location.blockX},${p.location.blockY},${p.location.blockZ}"
            val op = if (p.isOp) " *" else ""
            "${Term.pad(p.name, 18)} ${Term.pad(world, 16)} ($loc)$op"
        }
    }
}
