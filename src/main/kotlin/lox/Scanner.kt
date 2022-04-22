package lox

import lox.TokenType.*

class Scanner(
    private val source: String
) {
    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }

        tokens.add(Token(EndOfFile, "", null, line))
        return tokens
    }


    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    private val isAtEnd get() = current >= source.length

    private fun scanToken() {
        val char = advance()

        when (char) {
            '(' -> addToken(LeftParen)
            ')' -> addToken(RightParen)
            '{' -> addToken(LeftBrace)
            '}' -> addToken(RightBrace)
            '.' -> addToken(Dot)
            ',' -> addToken(Comma)
            '+' -> addToken(Plus)
            '-' -> addToken(Minus)
            ';' -> addToken(Semicolon)
            '*' -> addToken(Asterisk)

            '!' -> addToken(if (match('=')) BangEqual else Bang)
            '=' -> addToken(if (match('=')) EqualEqual else Equal)
            '<' -> addToken(if (match('=')) LessEqual else Less)
            '>' -> addToken(if (match('=')) GreaterEqual else Greater)

            '/' -> {
                if (match('/')) {
                    advanceUntil('\n')
                } else {
                    addToken(Slash)
                }
            }

            ' ', '\r', '\t' -> {}

            '\n' -> line++

            '"' -> matchString()

            in digits -> matchNumber()

            in letters, '_' -> matchIdentifier()

            else -> error(line, "Unexpected character: '$char'")
        }
    }

    private fun matchString() {
        advanceUntil('"') { char ->
            if (char == '\n') {line++}
        }

        if (isAtEnd) {
            error(line, "Unterminated string.")
            return
        }

        advance() // The closing quote.

        val content = source.substring(start + 1, current - 1)
        addToken(TokenType.String, content)
    }

    private fun matchNumber() {
        advanceWhile(digits)

        if (peek() == '.' && peek(+1) in digits) {
            advance()

            advanceWhile(digits)
        }

        addToken(Number, source.substring(start, current).toDouble())
    }

    private fun matchIdentifier() {
        advanceWhile(letters + digits + '_')

        val text = source.substring(start, current)
        val type = keywords[text] ?: Identifier
        addToken(type)
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd) {return false}
        if (source[current] != expected) {return false}

        current++
        return true
    }

    private fun peek(offset: Int = 0) = source.getOrNull(current + offset)

    private fun advanceUntil(char: Char, block: (Char) -> Unit = {}) = advanceUntil(char..char, block)
    private fun advanceUntil(chars: Iterable<Char>, block: (Char) -> Unit = {}) {
        while (peek() !in chars && !isAtEnd) {
            block(peek()!!)
            advance()
        }
    }

    private fun advanceWhile(char: Char, block: (Char) -> Unit = {}) = advanceWhile(char..char, block)
    private fun advanceWhile(chars: Iterable<Char>, block: (Char) -> Unit = {}) {
        while (peek() in chars && !isAtEnd) {
            block(peek()!!)
            advance()
        }
    }

    companion object {
        private val digits = '0'..'9'
        private val letters = ('a'..'z') + ('A'..'Z')

        private val keywords = mapOf(
            "nil" to Nil,
            "true" to True,
            "false" to False,
            "and" to And,
            "or" to Or,
            "if" to If,
            "else" to Else,
            "for" to For,
            "while" to While,
            "return" to Return,
            "var" to Var,
            "fun" to Fun,
            "class" to Class,
            "this" to This,
            "super" to Super,
            "print" to Print
        )
    }
}
