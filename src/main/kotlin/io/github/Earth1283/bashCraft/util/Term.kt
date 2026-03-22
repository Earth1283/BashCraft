package io.github.Earth1283.bashCraft.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

object Term {

    fun out(text: String): Component =
        Component.text(text, NamedTextColor.GRAY)

    fun err(text: String): Component =
        Component.text("bash: $text", NamedTextColor.RED)

    fun ok(text: String): Component =
        Component.text(text, NamedTextColor.GREEN)

    fun dir(text: String): Component =
        Component.text(text, NamedTextColor.AQUA).decorate(TextDecoration.BOLD)

    fun hdr(text: String): Component =
        Component.text(text, NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)

    fun warn(text: String): Component =
        Component.text(text, NamedTextColor.GOLD)

    fun info(text: String): Component =
        Component.text(text, NamedTextColor.WHITE)

    fun dim(text: String): Component =
        Component.text(text, NamedTextColor.DARK_GRAY)

    fun prompt(name: String, path: String): Component =
        Component.text(name, NamedTextColor.GREEN, TextDecoration.BOLD)
            .append(Component.text("@bashcraft", NamedTextColor.GREEN))
            .append(Component.text(":", NamedTextColor.WHITE))
            .append(Component.text(path, NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("$ ", NamedTextColor.WHITE))

    /** Colorize a single line based on content heuristics. */
    fun colorize(line: String): Component {
        val t = line.trimStart()
        return when {
            // Unix-style error messages
            t.startsWith("bash: ") || t.startsWith("grep: ") || t.startsWith("ls: ") ||
            t.startsWith("cat: ") || t.startsWith("cd: ") || t.startsWith("find: ") ||
            t.startsWith("rm: ") || t.startsWith("mv: ") || t.startsWith("cp: ") ||
            t.contains("No such file") || t.contains("No such directory") ||
            t.contains("No such world") || t.contains("Permission denied") ||
            t.contains("command not found") || t.contains("cannot access") ||
            t.contains("Is a directory") ->
                Component.text(line, NamedTextColor.RED)

            // Section headers  ===  or  --- or  ***
            (t.startsWith("===") && t.endsWith("===")) ||
            (t.startsWith("---") && t.endsWith("---")) ->
                Component.text(line, NamedTextColor.YELLOW, TextDecoration.BOLD)

            // ls -l: directory entries start with 'd'
            t.matches(Regex("d[rwx-]{9}.*")) ->
                Component.text(line, NamedTextColor.AQUA)

            // ls -l: symlink entries
            t.matches(Regex("l[rwx-]{9}.*")) ->
                Component.text(line, NamedTextColor.LIGHT_PURPLE)

            // ls -l: file entries  (-rw...) — executable in green
            t.matches(Regex("-[rwx-]{9}.*x.*")) ->
                Component.text(line, NamedTextColor.GREEN)

            // ls -l: regular file
            t.matches(Regex("-[rwx-]{9}.*")) ->
                Component.text(line, NamedTextColor.WHITE)

            // "total N" lines produced by ls -l
            t.matches(Regex("total .*")) ->
                Component.text(line, NamedTextColor.DARK_GRAY)

            // Bare directory name (ends with /, no spaces) — short ls output
            !t.contains(' ') && t.endsWith("/") ->
                Component.text(line, NamedTextColor.AQUA, TextDecoration.BOLD)

            // Named tags  [OP]  [PLAYER]  [env]  [creative]  etc.
            t.contains(Regex("\\[[A-Za-z0-9_ ]+\\]")) ->
                Component.text(line, NamedTextColor.GREEN)

            // Header-ish labels ending with colon, no spaces (e.g. "Players:" "Blocks:")
            t.endsWith(":") && !t.contains(' ') ->
                Component.text(line, NamedTextColor.YELLOW)

            // Section lines that look like headers (all caps / short labels)
            t.matches(Regex("[A-Z ]{4,}")) ->
                Component.text(line, NamedTextColor.YELLOW)

            // Numbered lines — grep -n, history
            t.matches(Regex("\\d+[:\\t].*")) ->
                Component.text(line, NamedTextColor.WHITE)

            // Prompt line — don't re-color, keep as-is (already a Component)
            else -> Component.text(line, NamedTextColor.GRAY)
        }
    }

    fun send(sender: CommandSender, lines: List<String>) {
        lines.forEach { sender.sendMessage(colorize(it)) }
    }

    fun sendRaw(sender: CommandSender, comps: List<Component>) {
        comps.forEach { sender.sendMessage(it) }
    }

    fun pad(s: String, width: Int): String = s.padEnd(width).let {
        if (it.length > width) it.take(width) else it
    }

    fun humanBytes(bytes: Long): String = when {
        bytes < 0 -> "?"
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)}K"
        bytes < 1024L * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024))}M"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024 * 1024))}G"
    }

    fun formatDuration(seconds: Long): String {
        val d = seconds / 86400
        val h = (seconds % 86400) / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return when {
            d > 0 -> "${d}d ${h}h ${m}m"
            h > 0 -> "${h}h ${m}m ${s}s"
            m > 0 -> "${m}m ${s}s"
            else -> "${s}s"
        }
    }
}
