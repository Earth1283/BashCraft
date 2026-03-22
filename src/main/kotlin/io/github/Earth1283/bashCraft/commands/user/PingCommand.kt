package io.github.Earth1283.bashCraft.commands.user

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PingCommand : LinuxCommand("ping", "Show player latency", "ping [player]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val target = when {
            args.isNotEmpty() -> Bukkit.getPlayer(args[0]) ?: return listOf("ping: ${args[0]}: Name or service not known")
            sender is Player -> sender
            else -> return listOf("ping: specify a player name")
        }

        val ping = target.ping
        val quality = when {
            ping < 50 -> "Excellent"
            ping < 100 -> "Good"
            ping < 200 -> "Fair"
            ping < 500 -> "Poor"
            else -> "Very Poor"
        }
        val bar = buildString {
            val filled = ((1.0 - (ping.coerceAtMost(500)) / 500.0) * 10).toInt()
            repeat(filled.coerceIn(0, 10)) { append('█') }
            repeat(10 - filled.coerceIn(0, 10)) { append('░') }
        }

        return listOf(
            "PING ${target.name} (${target.address?.address?.hostAddress ?: "unknown"}): 56 data bytes",
            "64 bytes from ${target.name}: time=${ping}ms",
            "",
            "--- ${target.name} ping statistics ---",
            "rtt min/avg/max = ${ping}/${ping}/${ping} ms  [$bar] $quality",
        )
    }
}
