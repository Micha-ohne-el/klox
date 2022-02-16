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


private fun runFile(path: String) {
    val content = File(path).readText()
    run(content)

    if (hadError) {
        exitProcess(65)
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

    for (token in tokens) {
        println(token)
    }
}

private fun report(line: Int, location: String, message: String) {
    System.err.println("[line $line] Error$location: $message")
    hadError = true
}

private var hadError = false