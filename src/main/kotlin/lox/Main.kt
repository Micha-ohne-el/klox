package lox

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun error(token: Token, message: String) {
    if (token.type == TokenType.EndOfFile) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}

fun error(error: RuntimeError) {
    System.err.println("[line ${error.token.line}] Runtime Error: ${error.message}")
    hadRuntimeError = true
}


private var hadError = false
private var hadRuntimeError = false

private val interpreter = Interpreter()

private fun runFile(path: String) {
    val content = File(path).readText()
    run(content)

    if (hadError) {
        exitProcess(65)
    }
    if (hadRuntimeError) {
        exitProcess(70)
    }
}

private fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")

        val line = reader.readLine() ?: break

        run(line)
        hadError = false
    }
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val statements = parser.parse()

    if (hadError) {return}

    interpreter.interpret(statements)
}

private fun report(line: Int, location: String, message: String) {
    System.err.println("[line $line] Error$location: $message")
    hadError = true
}
