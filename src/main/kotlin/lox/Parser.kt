package lox

import lox.TokenType.*

class Parser(
    private val tokens: List<Token>
) {
    fun parse(): Expression? {
        try {
            return parseExpression()
        } catch (error: ParseError) {
            return null
        }
    }


    private var current = 0

    private val previous get() = tokens[current - 1]

    private val isAtEnd get() = peek().type == EndOfFile

    private fun parseExpression(): Expression {
        return parseEquality()
    }

    private fun parseEquality(): Expression {
        var expression = parseComparison()

        while (match(BangEqual, EqualEqual)) {
            val operator = previous
            val right = parseComparison()
            expression = Expression.Binary(expression, operator, right)
        }

        return expression
    }

    private fun parseComparison(): Expression {
        var expression = parseTerm()

        while (match(Greater, GreaterEqual, Less, LessEqual)) {
            val operator = previous
            val right = parseTerm()
            expression = Expression.Binary(expression, operator, right)
        }

        return expression
    }

    private fun parseTerm(): Expression {
        var expression = parseFactor()

        while (match(Plus, Minus)) {
            val operator = previous
            val right = parseFactor()
            expression = Expression.Binary(expression, operator, right)
        }

        return expression
    }

    private fun parseFactor(): Expression {
        var expression = parsePrefix()

        while (match(Asterisk, Slash)) {
            val operator = previous
            val right = parsePrefix()
            expression = Expression.Binary(expression, operator, right)
        }

        return expression
    }

    private fun parsePrefix(): Expression {
        if (match(Bang, Minus)) {
            val operator = previous
            val right = parsePrefix()
            return Expression.Prefix(operator, right)
        }

        return parsePrimary()
    }

    private fun parsePrimary(): Expression {
        if (match(False)) {return Expression.Literal(false)}
        if (match(True)) {return Expression.Literal(true)}
        if (match(Nil)) {return Expression.Literal(null)}

        if (match(Number, TokenType.String)) {
            return Expression.Literal(previous.literal)
        }

        if (match(LeftParen)) {
            val expression = parseExpression()
            consume(RightParen, "Expect ')' after expression.")

            return Expression.Grouping(expression)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd) {return false}

        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd) {current++}

        return previous
    }

    private fun peek(offset: Int = 0) = tokens[current + offset]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
            return advance()
        }

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        lox.error(token, message)

        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd) {
            if (previous.type == Semicolon) {return}

            when (peek().type) {
                If, For, While, Return, Var, Fun, Class, Print -> return
            }

            advance()
        }
    }

    private class ParseError : RuntimeException()
}
