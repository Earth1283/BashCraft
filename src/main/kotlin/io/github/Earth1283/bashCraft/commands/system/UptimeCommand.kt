package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.BashCraft
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.ServerUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class UptimeCommand : LinuxCommand("uptime", "Tell how long the server has been running", "uptime") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val now = java.time.LocalTime.now()
        val timeStr = "%02d:%02d:%02d".format(now.hour, now.minute, now.second)

        val upSecs = if (BashCraft.startTime > 0) (System.currentTimeMillis() - BashCraft.startTime) / 1000 else 0L
        val upStr = Term.formatDuration(upSecs)

        val players = Bukkit.getOnlinePlayers().size
        val playerStr = if (players == 1) "1 player" else "$players players"

        val tps = ServerUtil.getTPS()
        val loadStr = tps.take(3).joinToString(", ") { "%.2f".format(it.coerceAtMost(20.0)) }

        return listOf(" $timeStr  up $upStr,  $playerStr,  load average: $loadStr")
    }
}
