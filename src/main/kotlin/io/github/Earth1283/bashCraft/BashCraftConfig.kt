package io.github.Earth1283.bashCraft

import java.io.File
import net.kyori.adventure.text.format.NamedTextColor

object BashCraftConfig {

    // Session behavior
    var historySize: Int = 500
    var historyDefaultLines: Int = 50
    var promptMode: Boolean = false

    // Output guards
    var clearLines: Int = 50
    var yesLimit: Int = 20
    var bannerMaxChars: Int = 12
    var cowsayWrapWidth: Int = 38
    var pipeMaxStages: Int = 10

    // Feature toggles
    var fortuneEnabled: Boolean = true

    // Color scheme
    var colorError: NamedTextColor = NamedTextColor.RED
    var colorSuccess: NamedTextColor = NamedTextColor.GREEN
    var colorDirectory: NamedTextColor = NamedTextColor.AQUA
    var colorHeader: NamedTextColor = NamedTextColor.YELLOW
    var colorWarning: NamedTextColor = NamedTextColor.GOLD
    var colorInfo: NamedTextColor = NamedTextColor.WHITE
    var colorDim: NamedTextColor = NamedTextColor.DARK_GRAY

    // Session defaults
    var defaultVariables: Map<String, String> = fallbackVars()
    var defaultAliases: Map<String, String> = fallbackAliases()

    private fun fallbackVars() = mapOf(
        "SHELL" to "/bin/bash",
        "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "EDITOR" to "vi",
        "TERM" to "xterm-256color",
        "LANG" to "en_US.UTF-8",
    )

    private fun fallbackAliases() = mapOf(
        "ll" to "ls -la",
        "la" to "ls -a",
        "l" to "ls -CF",
        "grep" to "grep --color=auto",
    )

    private fun parseColor(name: String): NamedTextColor {
        val color = NamedTextColor.NAMES.value(name.lowercase())
        if (color == null) {
            BashCraft.instance?.logger?.warning("bashcraftrc: unknown color '$name', falling back to GRAY")
        }
        return color ?: NamedTextColor.GRAY
    }

    fun load(dataFolder: File) {
        // Reset to hardcoded defaults before each load (supports clean reload)
        historySize = 500; historyDefaultLines = 50; promptMode = false
        clearLines = 50; yesLimit = 20; bannerMaxChars = 12
        cowsayWrapWidth = 38; pipeMaxStages = 10
        fortuneEnabled = true
        colorError = NamedTextColor.RED; colorSuccess = NamedTextColor.GREEN
        colorDirectory = NamedTextColor.AQUA; colorHeader = NamedTextColor.YELLOW
        colorWarning = NamedTextColor.GOLD; colorInfo = NamedTextColor.WHITE
        colorDim = NamedTextColor.DARK_GRAY
        val vars = fallbackVars().toMutableMap()
        val aliases = fallbackAliases().toMutableMap()

        val rcFile = File(dataFolder, "bashcraftrc")
        if (!rcFile.exists()) {
            defaultVariables = vars
            defaultAliases = aliases
            return
        }

        for (raw in rcFile.readLines()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            when {
                line.startsWith("export ") -> {
                    val eq = line.indexOf('=', 7)
                    if (eq < 0) continue
                    val key = line.substring(7, eq).trim()
                    val value = line.substring(eq + 1).trim()
                        .removeSurrounding("\"").removeSurrounding("'")
                    if (key.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))) {
                        vars[key] = value
                    } else {
                        BashCraft.instance?.logger?.warning("bashcraftrc: invalid variable name '$key'")
                    }
                }
                line.startsWith("alias ") -> {
                    val eq = line.indexOf('=', 6)
                    if (eq < 0) continue
                    val name = line.substring(6, eq).trim()
                    val value = line.substring(eq + 1)
                    if (name.isNotEmpty()) aliases[name] = value
                }
                else -> {
                    val eq = line.indexOf('=')
                    if (eq < 0) continue
                    val key = line.substring(0, eq).trim()
                    val value = line.substring(eq + 1).trim()
                    when (key) {
                        "history_size"          -> historySize = value.toIntOrNull() ?: 500
                        "history_default_lines" -> historyDefaultLines = value.toIntOrNull() ?: 50
                        "prompt_mode"           -> promptMode = value.equals("true", ignoreCase = true)
                        "clear_lines"           -> clearLines = (value.toIntOrNull() ?: 50).coerceIn(1, 200)
                        "yes_limit"             -> yesLimit = (value.toIntOrNull() ?: 20).coerceIn(1, 100)
                        "banner_max_chars"      -> bannerMaxChars = (value.toIntOrNull() ?: 12).coerceIn(1, 20)
                        "cowsay_wrap_width"     -> cowsayWrapWidth = (value.toIntOrNull() ?: 38).coerceIn(10, 60)
                        "pipe_max_stages"       -> pipeMaxStages = (value.toIntOrNull() ?: 10).coerceAtLeast(0)
                        "fortune_enabled"       -> fortuneEnabled = value.equals("true", ignoreCase = true)
                        "color_error"           -> colorError = parseColor(value)
                        "color_success"         -> colorSuccess = parseColor(value)
                        "color_directory"       -> colorDirectory = parseColor(value)
                        "color_header"          -> colorHeader = parseColor(value)
                        "color_warning"         -> colorWarning = parseColor(value)
                        "color_info"            -> colorInfo = parseColor(value)
                        "color_dim"             -> colorDim = parseColor(value)
                        else -> BashCraft.instance?.logger?.warning("bashcraftrc: unknown key '$key'")
                    }
                }
            }
        }

        defaultVariables = vars
        defaultAliases = aliases
        BashCraft.instance?.logger?.info(
            "BashCraftConfig loaded: ${vars.size} env vars, ${aliases.size} aliases"
        )
    }
}
