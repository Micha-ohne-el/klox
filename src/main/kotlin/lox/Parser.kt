package lox

import lox.TokenType.*
import lox.expressions.*
import lox.statements.*

class Parser(
    private val tokens: List<Token>
) {
    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!isAtEnd) {
            statements.add(parseDeclaration() ?: continue)
        }

        return statements
    }


    private var current = 0

    private val previous get() = tokens[current - 1]

    private val isAtEnd get() = peek().type == EndOfFile

    private fun parseDeclaration(): Statement? {
        try {
            if (match(Var)) {
                return parseVarDeclaration()
            }

            return parseStatement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun parseVarDeclaration(): Statement {
        val name = consume(Identifier, "Expect variable name.")

        val initializer = if (match(Equal)) {
            parseExpression()
        } else {
            null
        }

        consume(Semicolon, "Expect ';' after variable declaration.")

        return VariableStatement(name, initializer)
    }

    private fun parseStatement(): Statement {
        if (match(If)) {
            return parseIfStatement()
        }

        if (match(While)) {
            return parseWhileStatement()
        }

        if (match(For)) {
            return parseForStatement()
        }

        if (match(Print)) {
            return parsePrintStatement()
        }

        if (match(LeftBrace)) {
            return BlockStatement(parseBlockStatement())
        }

        return parseExpressionStatement()
    }

    private fun parseIfStatement(): Statement {
        consume(LeftParen, "Expect '(' after 'if'.")

        val condition = parseExpression()

        consume(RightParen, "Expect ')' after condition.")

        val thenBranch = parseStatement()

        val elseBranch = if (match(Else)) {
            parseStatement()
        } else {
            null
        }

        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Statement {
        consume(LeftParen, "Expect '(' after 'while'.")

        val condition = parseExpression()

        consume(RightParen, "Expect ')' after condition.")

        val statement = parseStatement()

        return WhileStatement(condition, statement)
    }

    private fun parseForStatement(): Statement {
        consume(LeftParen, "Expect '(' after 'for'.")

        val initializer = if (match(Semicolon)) {
            null
        } else if (match(Var)) {
            parseVarDeclaration()
        } else {
            parseExpressionStatement()
        }

        val condition = if (!check(Semicolon)) {
            parseExpression()
        } else {
            LiteralExpression(true)
        }

        consume(Semicolon, "Expect ';' after condition.")

        val increment = if (!check(RightParen)) {
            parseExpression()
        } else {
            null
        }

        consume(RightParen, "Expect ')' after for clauses.")

        var body = parseStatement()

        if (increment != null) {
            body = BlockStatement(listOf(body, ExpressionStatement(increment)))
        }

        body = WhileStatement(condition, body)

        if (initializer != null) {
            body = BlockStatement(listOf(initializer, body))
        }

        return body
    }

    private fun parsePrintStatement(): Statement {
        val expression = parseExpression()

        consume(Semicolon, "Expect ';' after statement.")

        return PrintStatement(expression)
    }

    private fun parseBlockStatement(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!check(RightBrace) && !isAtEnd) {
            statements.add(parseDeclaration() ?: continue)
        }

        consume(RightBrace, "Expect '}' after block.")

        return statements
    }

    private fun parseExpressionStatement(): Statement {
        val expression = parseExpression()

        consume(Semicolon, "Expect ';' after expression.")

        return ExpressionStatement(expression)
    }

    private fun parseExpression(): Expression {
        return parseAssignment()
    }

    private fun parseAssignment(): Expression {
        val expression = parseOr()

        if (match(Equal)) {
            val equals = previous
            val value = parseAssignment()

            if (expression is VariableExpression) {
                return AssignmentExpression(expression.name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expression
    }

    private fun parseOr(): Expression {
        var expression = parseAnd()

        while (match(Or)) {
            val operator = previous
            val right = parseAnd()

            expression = ShortingExpression(expression, operator, right)
        }

        return expression
    }

    private fun parseAnd(): Expression {
        var expression = parseEquality()

        while (match(And)) {
            val operator = previous
            val right = parseEquality()

            expression = ShortingExpression(expression, operator, right)
        }

        return expression
    }

    private fun parseEquality(): Expression {
        var expression = parseComparison()

        while (match(BangEqual, EqualEqual)) {
            val operator = previous
            val right = parseComparison()
            expression = BinaryExpression(expression, operator, right)
        }

        return expression
    }

    private fun parseComparison(): Expression {
        var expression = parseTerm()

        while (match(Greater, GreaterEqual, Less, LessEqual)) {
            val operator = previous
            val right = parseTerm()
            expression = BinaryExpression(expression, operator, right)
        }

        return expression
    }

    private fun parseTerm(): Expression {
        var expression = parseFactor()

        while (match(Plus, Minus)) {
            val operator = previous
            val right = parseFactor()
            expression = BinaryExpression(expression, operator, right)
        }

        return expression
    }

    private fun parseFactor(): Expression {
        var expression = parsePrefix()

        while (match(Asterisk, Slash)) {
            val operator = previous
            val right = parsePrefix()
            expression = BinaryExpression(expression, operator, right)
        }

        return expression
    }

    private fun parsePrefix(): Expression {
        if (match(Bang, Minus)) {
            val operator = previous
            val right = parsePrefix()
            return PrefixExpression(operator, right)
        }

        return parsePrimary()
    }

    private fun parsePrimary(): Expression {
        if (match(False)) {return LiteralExpression(false)}
        if (match(True)) {return LiteralExpression(true)}
        if (match(Nil)) {return LiteralExpression(null)}

        if (match(Number, TokenType.String)) {
            return LiteralExpression(previous.literal)
        }

        if (match(Identifier)) {
            return VariableExpression(previous)
        }

        if (match(LeftParen)) {
            val expression = parseExpression()
            consume(RightParen, "Expect ')' after expression.")

            return GroupingExpression(expression)
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
