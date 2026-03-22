package io.github.Earth1283.bashCraft.commands

import io.github.Earth1283.bashCraft.SessionManager
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.util.Term
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class FsCommand : Command("fs", "Toggle real filesystem mode", "/fs [on|off|status|tree]", emptyList()) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        val session = SessionManager.getOrCreate(sender)
        when (args.firstOrNull()?.lowercase()) {
            "on", "enable" -> enableFs(sender, session)
            "off", "disable" -> disableFs(sender, session)
            "tree" -> showTree(sender, session, args.getOrNull(1))
            "status", null -> showStatus(sender, session)
            else -> sender.sendMessage(Term.err("fs: unknown subcommand '${args[0]}'. Use: on, off, status, tree"))
        }
        return true
    }

    private fun enableFs(sender: CommandSender, session: TerminalSession) {
        if (!sender.hasPermission("bashcraft.fs")) {
            sender.sendMessage(Term.err("fs: Permission denied (requires bashcraft.fs)"))
            return
        }
        session.fsMode = true
        session.promptMode = true
        val home = System.getProperty("user.dir")
        session.workingPath = home
        session.variables["HOME"] = home
        session.variables["PWD"] = home

        sender.sendMessage(Term.ok("Switched to filesystem mode."))
        sender.sendMessage(Term.out("  Home: $home"))
        sender.sendMessage(Term.warn("  WARNING: You have access to the real server filesystem."))
        sender.sendMessage(Term.warn("  Destructive operations (rm, mv) require bashcraft.fs.write."))
        sender.sendMessage(Term.out("  Type 'ls' to list files, 'cd' to navigate, 'fs off' to return."))
        sender.sendMessage(Term.prompt(sender.name, session.workingPath))
    }

    private fun disableFs(sender: CommandSender, session: TerminalSession) {
        session.fsMode = false
        val player = sender as? Player
        session.workingPath = player?.let { "/${it.world.name}" } ?: "/"
        sender.sendMessage(Term.ok("Returned to Minecraft mode. Working path: ${session.workingPath}"))
        if (session.promptMode) sender.sendMessage(Term.prompt(sender.name, session.workingPath))
    }

    private fun showStatus(sender: CommandSender, session: TerminalSession) {
        val mode = if (session.fsMode) "filesystem (real)" else "Minecraft virtual"
        sender.sendMessage(Term.out("Mode:    $mode"))
        sender.sendMessage(Term.out("Path:    ${session.workingPath}"))
        sender.sendMessage(Term.out("Prompt:  ${if (session.promptMode) "enabled" else "disabled"}"))
        if (!session.fsMode) {
            sender.sendMessage(Term.dim("Use '/fs on' to enter real filesystem mode."))
        }
    }

    private fun showTree(sender: CommandSender, session: TerminalSession, pathArg: String?) {
        val rootStr = if (pathArg != null) {
            if (session.fsMode) {
                val base = Paths.get(session.workingPath)
                if (pathArg.startsWith("/")) pathArg else base.resolve(pathArg).normalize().toAbsolutePath().toString()
            } else session.workingPath
        } else session.workingPath

        if (!session.fsMode) {
            showMinecraftTree(sender)
            return
        }

        val root = Paths.get(rootStr)
        if (!Files.exists(root)) {
            sender.sendMessage(Term.err("fs: '$rootStr': No such file or directory"))
            return
        }

        sender.sendMessage(Term.dir(root.toString()))
        printTree(sender, root, "", 0, 4)
    }

    private fun printTree(sender: CommandSender, dir: java.nio.file.Path, prefix: String, depth: Int, maxDepth: Int) {
        if (depth >= maxDepth) return
        val entries = try {
            Files.list(dir).use { it.sorted(Comparator.comparing { p: java.nio.file.Path ->
                (!Files.isDirectory(p)).toString() + p.fileName.toString()
            }).toList() }
        } catch (_: Exception) { return }

        entries.forEachIndexed { i, path ->
            val last = i == entries.lastIndex
            val connector = if (last) "└── " else "├── "
            val name = path.fileName.toString()
            val isDir = Files.isDirectory(path)
            val displayName = if (isDir) "$name/" else name
            val color = if (isDir) NamedTextColor.AQUA else NamedTextColor.GRAY
            sender.sendMessage(Component.text("$prefix$connector", NamedTextColor.DARK_GRAY)
                .append(Component.text(displayName, color)))
            if (isDir) {
                val childPrefix = prefix + if (last) "    " else "│   "
                printTree(sender, path, childPrefix, depth + 1, maxDepth)
            }
        }
    }

    private fun showMinecraftTree(sender: CommandSender) {
        sender.sendMessage(Term.dir("/"))
        val worlds = org.bukkit.Bukkit.getWorlds()
        worlds.forEachIndexed { i, world ->
            val last = i == worlds.lastIndex
            val connector = if (last) "└── " else "├── "
            val players = world.players.size
            val entities = world.entityCount
            val env = world.environment.name.lowercase()
            sender.sendMessage(
                Component.text(connector, NamedTextColor.DARK_GRAY)
                    .append(Component.text("${world.name}/", NamedTextColor.AQUA, net.kyori.adventure.text.format.TextDecoration.BOLD))
                    .append(Component.text("  [$env · ${players}p · ${entities}e]", NamedTextColor.GRAY))
            )
            if (!last) {
                val p = "│   "
                world.players.take(5).forEach { pl ->
                    sender.sendMessage(Component.text("$p├── ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(pl.name, NamedTextColor.GREEN)))
                }
                if (world.players.size > 5) {
                    sender.sendMessage(Component.text("$p└── ", NamedTextColor.DARK_GRAY)
                        .append(Component.text("... and ${world.players.size - 5} more", NamedTextColor.DARK_GRAY)))
                }
            }
        }
    }
}
