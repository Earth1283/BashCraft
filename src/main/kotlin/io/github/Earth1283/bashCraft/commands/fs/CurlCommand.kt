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

class CurlCommand : LinuxCommand("curl", "Transfer data from a URL", "curl [-o file] [-X METHOD] [-d body] [-H header] url") {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> =
        listOf("bash: curl: only available in filesystem mode (/fs on)")

    override fun runFs(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        if (!sender.hasPermission("bashcraft.fs.curl")) return listOf("curl: Permission denied (requires bashcraft.fs.curl)")

        // Parse args
        var outputFile: String? = null
        var method = "GET"
        var body: String? = null
        val headers = mutableMapOf<String, String>()
        var url: String? = null
        var verbose = false
        var silent = false

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-o", "--output" -> { i++; if (i < args.size) outputFile = args[i] }
                "-X", "--request" -> { i++; if (i < args.size) method = args[i].uppercase() }
                "-d", "--data" -> { i++; if (i < args.size) body = args[i] }
                "-H", "--header" -> {
                    i++; if (i < args.size) {
                        val h = args[i].split(":", limit = 2)
                        if (h.size == 2) headers[h[0].trim()] = h[1].trim()
                    }
                }
                "-v", "--verbose" -> verbose = true
                "-s", "--silent" -> silent = true
                "-L", "--location" -> {} // handled by default
                else -> if (!args[i].startsWith("-")) url = args[i]
            }
            i++
        }

        if (url == null) return listOf("curl: no URL specified")

        val saveToFile = outputFile != null && sender.hasPermission("bashcraft.fs.write")

        // Run async so we don't block the main thread
        val plugin = BashCraft.instance ?: return listOf("curl: plugin not initialized")
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val result = doRequest(url, method, body, headers, verbose)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (saveToFile) {
                    val player = sender as? Player
                    val filePath = PathUtil.resolve(session, outputFile, player)
                    try {
                        Files.writeString(Paths.get(filePath), result.body)
                        sender.sendMessage(Term.ok("  % Total: ${result.body.length} bytes -> $filePath"))
                    } catch (e: Exception) {
                        sender.sendMessage(Term.err("curl: cannot write to $filePath: ${e.message}"))
                    }
                } else {
                    if (!silent) {
                        if (verbose) {
                            sender.sendMessage(Term.dim("< HTTP ${result.statusCode} ${result.statusText}"))
                            result.headers.forEach { (k, v) -> sender.sendMessage(Term.dim("< $k: $v")) }
                            sender.sendMessage(Term.dim("<"))
                        }
                        result.body.lines().take(200).forEach { line ->
                            sender.sendMessage(Term.out(line))
                        }
                        if (result.body.lines().size > 200) {
                            sender.sendMessage(Term.warn("... (truncated to 200 lines)"))
                        }
                    }
                }
                if (session.promptMode) sender.sendMessage(Term.prompt(sender.name, session.workingPath))
            })
        })

        return listOf("  % Connecting to $url...")
    }

    private data class HttpResult(
        val statusCode: Int,
        val statusText: String,
        val headers: Map<String, String>,
        val body: String,
    )

    private fun doRequest(url: String, method: String, body: String?, headers: Map<String, String>, verbose: Boolean): HttpResult {
        return try {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build()

            val reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))

            headers.forEach { (k, v) -> reqBuilder.header(k, v) }

            if (body != null) {
                reqBuilder.method(method, HttpRequest.BodyPublishers.ofString(body))
            } else {
                reqBuilder.method(method, HttpRequest.BodyPublishers.noBody())
            }

            val response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString())
            val hdrs = response.headers().map().entries.associate { it.key to it.value.joinToString(", ") }
            HttpResult(response.statusCode(), "OK", hdrs, response.body())
        } catch (e: Exception) {
            HttpResult(0, "ERROR", emptyMap(), "curl: (${e.javaClass.simpleName}) ${e.message}")
        }
    }
}
