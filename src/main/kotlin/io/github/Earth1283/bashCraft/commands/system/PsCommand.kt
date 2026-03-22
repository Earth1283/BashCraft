package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.SessionManager
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PsCommand : LinuxCommand("ps", "Report process status (online players)", "ps [-aux]", listOf("processes")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, _) = parseFlags(args, "a", "u", "x", "e")
        val all = "a" in flags || "u" in flags || "x" in flags || "e" in flags || args.isEmpty()
        val players = Bukkit.getOnlinePlayers().toList()

        val lines = mutableListOf<String>()
        lines += "  ${Term.pad("PID", 7)} ${Term.pad("TTY", 16)} STAT  ${Term.pad("TIME", 10)} COMMAND"

        for (p in players) {
            val pid = p.entityId
            val world = p.world.name.take(16)
            val stat = gamemodeChar(p.gameMode) + if (p.isSleeping) "Z" else if (p.isFlying) "F" else " "
            val uptime = SessionManager.sessions[p.uniqueId.toString()]
                ?.let { Term.formatDuration((System.currentTimeMillis() - it.joinTime) / 1000) }
                ?: "?"
            val loc = p.location.let { "(${it.blockX},${it.blockY},${it.blockZ})" }
            val op = if (p.isOp) " [OP]" else ""
            lines += "  ${Term.pad(pid.toString(), 7)} ${Term.pad(world, 16)} $stat     ${Term.pad(uptime, 10)} ${p.name}$op $loc"
        }

        if (players.isEmpty()) lines += "  (no players online)"
        return lines
    }

    private fun gamemodeChar(gm: GameMode) = when (gm) {
        GameMode.SURVIVAL -> "S"
        GameMode.CREATIVE -> "C"
        GameMode.ADVENTURE -> "A"
        GameMode.SPECTATOR -> "R"
    }
}

