package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.ServerUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateCommand : LinuxCommand("date", "Print current date and time", "date [+format]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val now = ZonedDateTime.now()
        val fmt = args.firstOrNull()?.removePrefix("+")
            ?.replace("%Y", now.year.toString())
            ?.replace("%m", "%02d".format(now.monthValue))
            ?.replace("%d", "%02d".format(now.dayOfMonth))
            ?.replace("%H", "%02d".format(now.hour))
            ?.replace("%M", "%02d".format(now.minute))
            ?.replace("%S", "%02d".format(now.second))
            ?: DateTimeFormatter.RFC_1123_DATE_TIME.format(now)

        val lines = mutableListOf(fmt)

        // Minecraft time
        val world = (sender as? Player)?.world ?: Bukkit.getWorlds().firstOrNull()
        if (world != null) {
            val mcTime = ServerUtil.mcTimeToString(world.time)
            val day = (world.fullTime / 24000) + 1
            val moon = ServerUtil.moonPhase(world)
            lines += "MC time: Day $day, $mcTime  [${moon}]"
        }

        return lines
    }
}
