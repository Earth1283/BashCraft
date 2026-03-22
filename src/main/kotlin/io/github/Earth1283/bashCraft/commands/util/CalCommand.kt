package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.ServerUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CalCommand : LinuxCommand("cal", "Display Minecraft lunar calendar", "cal") {

    private val moonSymbols = arrayOf("○", "◑", "◑", "◑", "●", "◐", "◐", "◐")
    private val moonNames = arrayOf(
        "Full Moon   ", "Waning Gibb.", "Last Quarter", "Waning Cres.",
        "New Moon    ", "Waxing Cres.", "First Quart.", "Waxing Gibb."
    )

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val world = (sender as? Player)?.world ?: Bukkit.getWorlds().firstOrNull()
            ?: return listOf("cal: no world available")

        val fullTime = world.fullTime
        val currentDay = (fullTime / 24000).toInt() + 1
        val phase = ((currentDay - 1) % 8).coerceIn(0, 7)
        val mcTime = ServerUtil.mcTimeToString(world.time)

        val lines = mutableListOf<String>()
        lines += "    Minecraft Lunar Calendar"
        lines += "  Day  Mo Tu We Th Fr Sa Su"

        // Show 4 weeks of cycle
        val startDay = ((currentDay - 1) / 7) * 7 + 1
        for (week in 0 until 4) {
            val sb = StringBuilder()
            val weekStart = startDay + week * 7
            sb.append("  ${Term.pad(weekStart.toString(), 4)} ")
            for (d in 0 until 7) {
                val day = weekStart + d
                val p = ((day - 1) % 8)
                val sym = moonSymbols[p]
                val marker = if (day == currentDay) "[${sym}]" else " $sym "
                sb.append(marker)
            }
            lines += sb.toString()
        }

        lines += ""
        lines += "  Today: Day $currentDay  ${moonNames[phase]}  ${moonSymbols[phase]}"
        lines += "  MC time: $mcTime"
        return lines
    }
}

