package io.github.Earth1283.bashCraft

import io.github.Earth1283.bashCraft.commands.FsCommand
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.commands.filesystem.*
import io.github.Earth1283.bashCraft.commands.fs.*
import io.github.Earth1283.bashCraft.commands.process.*
import io.github.Earth1283.bashCraft.commands.system.*
import io.github.Earth1283.bashCraft.commands.text.*
import io.github.Earth1283.bashCraft.commands.user.*
import io.github.Earth1283.bashCraft.commands.util.*
import org.bukkit.Bukkit
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.java.JavaPlugin

object CommandRegistry {

    private val commands = mutableMapOf<String, LinuxCommand>()

    fun register(plugin: JavaPlugin) {
        // Get Bukkit's SimpleCommandMap via reflection
        val field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
        field.isAccessible = true
        val map = field.get(Bukkit.getServer()) as SimpleCommandMap

        // Pinnacle of human programmimg
        val linuxCommands: List<LinuxCommand> = listOf(
            // Filesystem (dual-mode)
            LsCommand(), PwdCommand(), CdCommand(), CatCommand(), FindCommand(), StatCommand(),
            // FS-mode-only
            MvCommand(), CpCommand(), TouchCommand(), RmCommand(), MkdirCommand(), CurlCommand(),
            // System info
            PsCommand(), TopCommand(), FreeCommand(), UnameCommand(), UptimeCommand(), DfCommand(), DuCommand(),
            // User/identity
            WhoamiCommand(), WhoCommand(), WCommand(), IdCommand(), PingCommand(),
            // Process control
            KillCommand(), KillallCommand(), SudoCommand(), SleepCommand(),
            // Text processing
            EchoCommand(), GrepCommand(), HeadCommand(), TailCommand(), SortCommand(),
            UniqCommand(), WcCommand(), TrCommand(), SedCommand(), CutCommand(),
            // Utilities
            DateCommand(), CalCommand(), HistoryCommand(), ClearCommand(), AliasCommand(),
            EnvCommand(), ExportCommand(), ManCommand(), BcCommand(), YesCommand(),
            FactorCommand(), RevCommand(), FortuneCommand(), CowsayCommand(), BannerCommand(),
        )

        // Register all LinuxCommand instances
        linuxCommands.forEach { cmd ->
            map.register("bashcraft", cmd)
            commands[cmd.name] = cmd
            cmd.aliases.forEach { alias -> commands[alias] = cmd }
        }

        // Register FsCommand separately (not a LinuxCommand)
        val fsCmd = FsCommand()
        map.register("bashcraft", fsCmd)

        plugin.logger.info("BashCraft: registered ${linuxCommands.size} commands + fs toggle")
    }

    fun get(name: String): LinuxCommand? =
        commands[name] ?: commands[name.removePrefix("bashcraft:")]
}
