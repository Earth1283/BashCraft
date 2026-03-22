package io.github.Earth1283.bashCraft.commands.process

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class KillallCommand : LinuxCommand("killall", "Kill all entities of a type", "killall <entityType>") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.kill")) return listOf("killall: Permission denied (requires bashcraft.kill)")
        if (args.isEmpty()) return listOf("killall: no process name specified")

        val typeName = args[0]
        val entityType = try {
            EntityType.valueOf(typeName.uppercase())
        } catch (_: Exception) {
            return listOf("killall: $typeName: no such entity type")
        }

        if (entityType == EntityType.PLAYER) {
            return listOf("killall: cannot kill all players (use kill @a)")
        }

        var killed = 0
        val world = (sender as? Player)?.world ?: run {
            // Console: kill in all worlds
            Bukkit.getWorlds().forEach { w ->
                w.entities.filter { it.type == entityType && it !is Player }.forEach { e ->
                    if (e is LivingEntity) e.health = 0.0 else e.remove()
                    killed++
                }
            }
            return listOf("killall: killed $killed ${typeName.lowercase()} (all worlds)")
        }

        world.entities.filter { it.type == entityType && it !is Player }.forEach { e ->
            if (e is LivingEntity) e.health = 0.0 else e.remove()
            killed++
        }

        return listOf("killall: killed $killed ${typeName.lowercase()} in ${world.name}")
    }
}
