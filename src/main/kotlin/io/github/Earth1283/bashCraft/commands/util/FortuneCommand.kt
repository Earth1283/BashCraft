package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender

class FortuneCommand : LinuxCommand("fortune", "Print a random fortune", "fortune") {

    private val fortunes = listOf(
        "Mine deep, but not below bedrock.",
        "The best sword is no sword — until you meet a creeper.",
        "Lava: nature's way of saying 'back up your inventory'.",
        "There is no shame in dying to fall damage. Again.",
        "With enough TNT, all problems are temporary.",
        "The Nether is not hell. Hell has better lighting.",
        "Endermen only want what they can't have — usually your face.",
        "A bed in the Nether teaches humility. And respawning.",
        "Fortune III on a pickaxe. Spend wisely.",
        "The sky limit was removed. So were your excuses.",
        "Phantoms are insomnia's way of punishing productivity.",
        "Every creeper explosion is an opportunity to redesign.",
        "Gravel: the free block with the most expensive timer.",
        "The End is a beginning. Wear Feather Falling.",
        "Not all who wander in caves are lost — most are just out of torches.",
        "When in doubt, punch the wood.",
        "Build upwards. Unless there's a lightning rod involved.",
        "Sheep are not infinite. Your wool demand is.",
        "A skeleton riding a spider is proof that the world hates you.",
        "sudo rm -rf /world — think twice, backup first.",
        "If it can be silktouched, it should be silkitouched.",
        "The real diamonds were the friends we mined along the way.",
        "You can't pipeline bedrock. Trust us, we tried.",
        "GOTO is harmful. So is lava swimming. Neither lesson sticks.",
        "Villagers have the economics of a haunted stock exchange.",
        "The warden sees with sound. Your WASD keys are loud.",
        "Hardcore mode: where every choice is final, like git push --force.",
        "The ocean monument is a lesson in patience and drowned harassment.",
        "A beacon in the sky is visible from 64 chunks. So is your hubris.",
        "You are not stuck. You are pathing.",
    )

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        return listOf(fortunes.random())
    }
}
