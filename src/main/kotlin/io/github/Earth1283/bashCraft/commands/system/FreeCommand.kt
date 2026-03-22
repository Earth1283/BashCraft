package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.command.CommandSender

class FreeCommand : LinuxCommand("free", "Display memory usage", "free [-m|-g|-h]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, _) = parseFlags(args, "m", "g", "h", "b", "k")
        val rt = Runtime.getRuntime()
        val max   = rt.maxMemory()
        val total = rt.totalMemory()
        val free  = rt.freeMemory()
        val used  = total - free
        val avail = max - used

        fun fmt(b: Long): String = when {
            "h" in flags -> Term.humanBytes(b)
            "g" in flags -> "${"%.1f".format(b / 1073741824.0)}G"
            "b" in flags -> b.toString()
            "k" in flags -> "${b / 1024}"
            else -> "${b / (1024 * 1024)}" // -m by default
        }

        val unit = when {
            "h" in flags -> ""
            "g" in flags -> "(GiB)"
            "b" in flags -> "(bytes)"
            "k" in flags -> "(kiB)"
            else -> "(MiB)"
        }

        return listOf(
            "              total        used        free      available",
            "Mem:   ${Term.pad(fmt(max), 12)} ${Term.pad(fmt(used), 12)} ${Term.pad(fmt(free), 12)} ${fmt(avail)}  $unit",
            "JVM heap allocated: ${fmt(total)} / ${fmt(max)} max",
        )
    }
}
