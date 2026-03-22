package io.github.Earth1283.bashCraft.commands.system

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.ServerUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class UnameCommand : LinuxCommand("uname", "Print system information", "uname [-a|-r|-s|-v|-n|-m]") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val (flags, _) = parseFlags(args, "a", "r", "s", "v", "n", "m", "o", "p", "i")
        val all = "a" in flags || flags.isEmpty()

        val sysname   = "Paper"                                          // -s
        val nodename  = ServerUtil.serverHostname()                       // -n
        val release   = Bukkit.getServer().version.substringBefore(" ")  // -r
        val version   = Bukkit.getVersion()                              // -v
        val machine   = System.getProperty("os.arch") ?: "unknown"       // -m
        val os        = System.getProperty("os.name") ?: "unknown"       // -o

        if (all || args.isEmpty()) {
            return listOf("$sysname $nodename $release $version $machine $os GNU/Linux")
        }

        val parts = mutableListOf<String>()
        if ("s" in flags) parts += sysname
        if ("n" in flags) parts += nodename
        if ("r" in flags) parts += release
        if ("v" in flags) parts += version
        if ("m" in flags || "p" in flags) parts += machine
        if ("o" in flags || "i" in flags) parts += "$os GNU/Linux"
        return listOf(parts.joinToString(" "))
    }
}
