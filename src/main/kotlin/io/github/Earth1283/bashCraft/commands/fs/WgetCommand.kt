package io.github.Earth1283.bashCraft.commands.fs

import io.github.Earth1283.bashCraft.BashCraft
import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import io.github.Earth1283.bashCraft.util.PathUtil
import io.github.Earth1283.bashCraft.util.Term
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Emulates `wget` — downloads a URL and saves it to a file (or prints to stdout with -O -).
 * FS mode only. Requires bashcraft.fs.wget; saving to disk also requires bashcraft.fs.write.
 *
 * Supported options:
 *   -O file / --output-document=file   write output to FILE ("-" prints to chat)
 *   -P dir  / --directory-prefix=dir   prepend DIR to the output path
 *   -q      / --quiet                  suppress progress messages
 *   --timeout=N                        connection/read timeout in seconds (default: 30)
 *   -U agent / --user-agent=agent      override the User-Agent header
 *   -c      / --continue               no-op stub (partial resume not supported)
 *   --no-check-certificate             no-op stub (TLS validation always on)
 */
class WgetCommand : LinuxCommand(
    "wget",
    "Download files from the web",
    "wget [-q] [-O file] [-P dir] [--timeout=N] url",
) {

    override fun run(
        sender: CommandSender,
        args: Array<String>,
        session: TerminalSession,
        pipedInput: List<String>?,
    ): List<String> = listOf("bash: wget: only available in filesystem mode (/fs on)")

    override fun runFs(
        sender: CommandSender,
        args: Array<String>,
        session: TerminalSession,
        pipedInput: List<String>?,
    ): List<String> {
        if (!sender.hasPermission("bashcraft.fs.wget"))
            return listOf("wget: Permission denied (requires bashcraft.fs.wget)")

        var outputFile: String? = null   // null → derive from URL; "-" → print to chat
        var prefixDir: String? = null
        var quiet = false
        var timeoutSecs = 30L
        var userAgent = "Wget/1.21 (linux-gnu)"
        var url: String? = null

        var i = 0
        while (i < args.size) {
            when (val arg = args[i]) {
                "-O", "--output-document" -> { i++; if (i < args.size) outputFile = args[i] }
                "-P", "--directory-prefix" -> { i++; if (i < args.size) prefixDir = args[i] }
                "-q", "--quiet" -> quiet = true
                "-c", "--continue" -> {}                     // stub: partial resume unsupported
                "--no-check-certificate" -> {}               // stub: TLS validation not bypassed
                else -> when {
                    arg.startsWith("--output-document=") -> outputFile = arg.removePrefix("--output-document=")
                    arg.startsWith("--directory-prefix=") -> prefixDir = arg.removePrefix("--directory-prefix=")
                    arg.startsWith("--timeout=") -> timeoutSecs = arg.removePrefix("--timeout=").toLongOrNull() ?: 30L
                    arg == "-U" -> { i++; if (i < args.size) userAgent = args[i] }
                    arg.startsWith("--user-agent=") -> userAgent = arg.removePrefix("--user-agent=")
                    !arg.startsWith("-") -> url = arg
                }
            }
            i++
        }

        if (url == null)
            return listOf("wget: missing URL", "Usage: wget [-q] [-O file] [-P dir] url")

        val printToChat = outputFile == "-"

        if (!printToChat && !sender.hasPermission("bashcraft.fs.write"))
            return listOf("wget: Permission denied (requires bashcraft.fs.write to save files)")

        val plugin = BashCraft.instance ?: return listOf("wget: plugin not initialized")

        val filenameFromUrl = runCatching { URI.create(url).path.substringAfterLast('/') }
            .getOrNull()?.ifBlank { "index.html" } ?: "index.html"

        val resolvedOutput = if (outputFile != null && !printToChat) outputFile else filenameFromUrl

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val result = doDownload(url, timeoutSecs, userAgent)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                when {
                    result.error != null ->
                        sender.sendMessage(Term.err("wget: $url: ${result.error}"))

                    printToChat -> {
                        val lines = result.body.lines()
                        lines.take(200).forEach { sender.sendMessage(Term.out(it)) }
                        if (lines.size > 200) sender.sendMessage(Term.warn("... (truncated to 200 lines)"))
                    }

                    else -> {
                        val player = sender as? Player
                        val base = if (prefixDir != null)
                            PathUtil.resolve(session, prefixDir, player)
                        else
                            session.workingPath
                        val finalPath = Paths.get(base, resolvedOutput)
                        try {
                            Files.createDirectories(finalPath.parent)
                            Files.writeString(finalPath, result.body)
                            if (!quiet) {
                                sender.sendMessage(
                                    Term.ok("'$finalPath' saved [${result.body.length} bytes]")
                                )
                            }
                        } catch (e: Exception) {
                            sender.sendMessage(Term.err("wget: cannot write '$finalPath': ${e.message}"))
                        }
                    }
                }
                if (session.promptMode) sender.sendMessage(Term.prompt(sender.name, session.workingPath))
            })
        })

        if (quiet) return emptyList()

        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val host = runCatching { URI.create(url).host }.getOrNull() ?: url
        return listOf(
            "--$ts--  $url",
            "Connecting to $host... connected.",
            "HTTP request sent, awaiting response...",
        )
    }

    private data class DownloadResult(val body: String, val error: String?)

    private fun doDownload(url: String, timeoutSecs: Long, userAgent: String): DownloadResult {
        return try {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(timeoutSecs))
                .build()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSecs))
                .header("User-Agent", userAgent)
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            DownloadResult(response.body(), null)
        } catch (e: Exception) {
            DownloadResult("", "(${e.javaClass.simpleName}) ${e.message}")
        }
    }
}
