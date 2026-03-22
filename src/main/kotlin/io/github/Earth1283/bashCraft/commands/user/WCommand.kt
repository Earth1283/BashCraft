package io.github.Earth1283.bashCraft.commands.user

import io.github.Earth1283.bashCraft.BashCraft
import io.github.Earth1283.bashCraft.SessionManager
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

class WCommand : LinuxCommand("w", "Show who is logged in and what they are doing", "w") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val players = Bukkit.getOnlinePlayers()
        val lines = mutableListOf<String>()

        val upSecs = if (BashCraft.startTime > 0) (System.currentTimeMillis() - BashCraft.startTime) / 1000 else 0L
        lines += " ${java.time.LocalTime.now().let { "%02d:%02d" .format(it.hour, it.minute) }}  up ${Term.formatDuration(upSecs)},  ${players.size} player(s)"
        lines += "${Term.pad("USER", 16)} ${Term.pad("TTY", 14)} ${Term.pad("FROM", 16)} ${Term.pad("LOGIN@", 8)} ${Term.pad("WHAT", 12)}"

        for (p in players) {
            val world = p.world.name.take(14)
            val gm = when (p.gameMode) {
                GameMode.SURVIVAL -> "survival"
                GameMode.CREATIVE -> "creative"
                GameMode.ADVENTURE -> "adventure"
                GameMode.SPECTATOR -> "spectator"
            }
            val session2 = SessionManager.sessions[p.uniqueId.toString()]
            val lastCmd = session2?.history?.lastOrNull()?.take(16) ?: "-"
            val ping = "${p.ping}ms"
            lines += "${Term.pad(p.name, 16)} ${Term.pad(world, 14)} ${Term.pad(ping, 16)} ${Term.pad("-", 8)} $gm: $lastCmd"
        }

        if (players.isEmpty()) lines += "(no players online)"
        return lines
    }
}
