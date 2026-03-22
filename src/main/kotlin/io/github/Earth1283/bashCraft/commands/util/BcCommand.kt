package io.github.Earth1283.bashCraft.commands.util

import io.github.Earth1283.bashCraft.TerminalSession
import io.github.Earth1283.bashCraft.commands.LinuxCommand
import org.bukkit.command.CommandSender
import kotlin.math.*

class BcCommand : LinuxCommand("bc", "Evaluate math expressions", "bc <expression>", listOf("calc", "expr")) {

    override fun run(sender: CommandSender, args: Array<String>, session: TerminalSession, pipedInput: List<String>?): List<String> {
        val expressions = pipedInput ?: args.toList().ifEmpty { return listOf("bc: no expression") }
        return expressions.map { expr ->
            try {
                val result = Parser(expr.trim()).parse()
                if (result == result.toLong().toDouble()) result.toLong().toString()
                else "%.10g".format(result).trimEnd('0').trimEnd('.')
            } catch (e: Exception) {
                "bc: error: ${e.message}"
            }
        }
    }

    private inner class Parser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpr()
            skipWs()
            if (pos < input.length) throw IllegalArgumentException("unexpected character: '${input[pos]}'")
            return result
        }

        private fun parseExpr(): Double {
            var result = parseTerm()
            while (pos < input.length) {
                skipWs()
                val op = input.getOrNull(pos) ?: break
                if (op != '+' && op != '-') break
                pos++
                val term = parseTerm()
                result = if (op == '+') result + term else result - term
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parsePow()
            while (pos < input.length) {
                skipWs()
                val op = input.getOrNull(pos) ?: break
                if (op != '*' && op != '/' && op != '%') break
                pos++
                val factor = parsePow()
                result = when (op) {
                    '*' -> result * factor
                    '/' -> if (factor == 0.0) throw ArithmeticException("division by zero") else result / factor
                    '%' -> result % factor
                    else -> result
                }
            }
            return result
        }

        private fun parsePow(): Double {
            val base = parseUnary()
            skipWs()
            return if (pos < input.length && input[pos] == '^') {
                pos++
                base.pow(parseUnary())
            } else base
        }

        private fun parseUnary(): Double {
            skipWs()
            if (pos < input.length && input[pos] == '-') { pos++; return -parsePrimary() }
            if (pos < input.length && input[pos] == '+') { pos++; return parsePrimary() }
            return parsePrimary()
        }

        private fun parsePrimary(): Double {
            skipWs()
            if (pos < input.length && input[pos] == '(') {
                pos++
                val result = parseExpr()
                skipWs()
                if (pos < input.length && input[pos] == ')') pos++
                else throw IllegalArgumentException("missing closing parenthesis")
                return result
            }

            // Functions: sqrt, abs, sin, cos, tan, log, exp, floor, ceil
            val fnMatch = Regex("(sqrt|abs|sin|cos|tan|log|ln|exp|floor|ceil|round)\\(").find(input.substring(pos))
            if (fnMatch != null && fnMatch.range.first == 0) {
                val fn = fnMatch.groupValues[1]
                pos += fn.length + 1
                val arg = parseExpr()
                skipWs()
                if (pos < input.length && input[pos] == ')') pos++
                return when (fn) {
                    "sqrt" -> sqrt(arg); "abs" -> abs(arg)
                    "sin" -> sin(arg); "cos" -> cos(arg); "tan" -> tan(arg)
                    "log" -> log10(arg); "ln" -> ln(arg); "exp" -> exp(arg)
                    "floor" -> floor(arg); "ceil" -> ceil(arg); "round" -> round(arg)
                    else -> arg
                }
            }

            // Constants
            if (input.substring(pos).startsWith("pi")) { pos += 2; return PI }
            if (input.substring(pos).startsWith("e") && (pos + 1 >= input.length || !input[pos + 1].isDigit())) {
                pos++; return E
            }

            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            if (pos == start) throw IllegalArgumentException("expected number at position $pos: '${input.substring(pos.coerceAtMost(input.length - 1))}'")
            return input.substring(start, pos).toDouble()
        }

        private fun skipWs() { while (pos < input.length && input[pos] == ' ') pos++ }
    }
}
