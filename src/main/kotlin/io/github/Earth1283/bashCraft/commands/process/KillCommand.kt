package io.github.Earth1283.bashCraft.commands.process

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class KillCommand : LinuxCommand("kill", "Terminate processes (players/entities)", "kill [-9] <player|selector>") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (args.isEmpty()) return listOf("kill: usage: kill [-9] <player|@selector>")

        var force = false
        val targets = mutableListOf<String>()
        for (a in args) {
            when (a) {
                "-9", "-KILL" -> force = true
                "-15", "-TERM" -> force = false
                else -> targets += a
            }
        }

        if (targets.isEmpty()) return listOf("kill: no target specified")

        val lines = mutableListOf<String>()
        for (target in targets) {
            lines += processTarget(sender, target, force)
        }
        return lines
    }

    private fun processTarget(sender: CommandSender, target: String, force: Boolean): String {
        if (target.startsWith("@")) {
            return killSelector(sender, target, force)
        }

        // Try as numeric entity ID
        val entityId = target.toIntOrNull()
        if (entityId != null) {
            val entity = Bukkit.getWorlds().flatMap { it.entities }.find { it.entityId == entityId }
                ?: return "kill: ($entityId) No such process"
            return killEntity(sender, entity, force)
        }

        // Try as player name
        val player = Bukkit.getPlayer(target) ?: return "kill: ($target) No such process"
        if (player == sender && !sender.hasPermission("bashcraft.kill.self")) {
            return "kill: permission denied"
        }
        if (player != sender && !sender.hasPermission("bashcraft.kill")) {
            return "kill: permission denied (requires bashcraft.kill)"
        }
        return killEntity(sender, player, force)
    }

    private fun killEntity(sender: CommandSender, entity: org.bukkit.entity.Entity, force: Boolean): String {
        if (entity !is LivingEntity) {
            entity.remove()
            return "killed ${entity.type.name.lowercase()}[${entity.entityId}]"
        }
        if (force) {
            entity.health = 0.0
        } else {
            entity.damage(entity.health + 1.0)
        }
        val name = if (entity is Player) entity.name else "${entity.type.name.lowercase()}[${entity.entityId}]"
        return "killed $name${if (force) " (SIGKILL)" else ""}"
    }

    private fun killSelector(sender: CommandSender, selector: String, force: Boolean): String {
        if (!sender.hasPermission("bashcraft.kill")) return "kill: permission denied (requires bashcraft.kill)"

        val typeMatch = selector.substringAfter("type=", "").substringBefore(",").substringBefore("]").lowercase()
        val nameMatch = selector.substringAfter("name=", "").substringBefore(",").substringBefore("]")

        val worlds = Bukkit.getWorlds()
        var killed = 0

        val entityType = if (typeMatch.isNotEmpty()) {
            try { EntityType.valueOf(typeMatch.uppercase()) } catch (_: Exception) { null }
        } else null

        for (world in worlds) {
            val entities = world.entities.filter { entity ->
                if (entity is Player && !sender.hasPermission("bashcraft.kill")) return@filter false
                val typeOk = entityType == null || entity.type == entityType
                val nameOk = nameMatch.isEmpty() || (entity is Player && entity.name.equals(nameMatch, ignoreCase = true))
                typeOk && nameOk
            }
            for (entity in entities) {
                if (entity is LivingEntity) {
                    if (force) entity.health = 0.0 else entity.damage(entity.health + 1.0)
                } else {
                    entity.remove()
                }
                killed++
            }
        }

        return "killed $killed entities matching '$selector'"
    }
}
