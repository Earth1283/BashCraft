package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.BashCraft
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.ServerUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

class TopCommand : LinuxCommand("top", "Display server processes and metrics", "top") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val rt = Runtime.getRuntime()
        val maxMem = rt.maxMemory() / (1024 * 1024)
        val totalMem = rt.totalMemory() / (1024 * 1024)
        val freeMem = rt.freeMemory() / (1024 * 1024)
        val usedMem = totalMem - freeMem

        val tps = ServerUtil.getTPS()
        val tpsStr = tps.take(3).joinToString(", ") { ServerUtil.formatTPS(it) }

        val upSecs = if (BashCraft.startTime > 0) (System.currentTimeMillis() - BashCraft.startTime) / 1000 else 0L
        val upStr = Term.formatDuration(upSecs)

        val players = Bukkit.getOnlinePlayers()
        val survival = players.count { it.gameMode == GameMode.SURVIVAL }
        val creative = players.count { it.gameMode == GameMode.CREATIVE }
        val spectator = players.count { it.gameMode == GameMode.SPECTATOR }

        val totalEntities = Bukkit.getWorlds().sumOf { it.entityCount }
        val totalChunks = Bukkit.getWorlds().sumOf { it.loadedChunks.size }

        val barLen = 20
        val usedBar = (usedMem.toDouble() / maxMem * barLen).toInt().coerceIn(0, barLen)
        val memBar = "[" + "█".repeat(usedBar) + "░".repeat(barLen - usedBar) + "]"

        val lines = mutableListOf<String>()
        lines += "bashcraft top - ${java.time.LocalTime.now().let { "%02d:%02d:%02d".format(it.hour, it.minute, it.second) }} up $upStr, ${players.size} players"
        lines += "Tasks: ${players.size + totalEntities} total, $survival survival, $creative creative, $spectator spectator"
        lines += "TPS: $tpsStr  (1m, 5m, 15m avg)"
        lines += "Mem: ${maxMem}MiB total, ${usedMem}MiB used, ${freeMem}MiB free  $memBar"
        lines += "Worlds: ${Bukkit.getWorlds().size}  Chunks loaded: $totalChunks  Entities: $totalEntities"
        lines += ""
        lines += "  ${Term.pad("PID", 7)} ${Term.pad("USER", 16)} ${Term.pad("WORLD", 14)} GM   LOC"

        val sorted = players.sortedBy { it.name }
        for (p in sorted) {
            val pid = p.entityId
            val world = p.world.name.take(14)
            val gm = when (p.gameMode) {
                GameMode.SURVIVAL -> "S"
                GameMode.CREATIVE -> "C"
                GameMode.ADVENTURE -> "A"
                GameMode.SPECTATOR -> "R"
            }
            val loc = p.location.let { "(${it.blockX},${it.blockY},${it.blockZ})" }
            val op = if (p.isOp) "*" else " "
            lines += "$op ${Term.pad(pid.toString(), 7)} ${Term.pad(p.name, 16)} ${Term.pad(world, 14)} $gm    $loc"
        }

        if (players.isEmpty()) lines += "  (no players online)"
        return lines
    }
}
