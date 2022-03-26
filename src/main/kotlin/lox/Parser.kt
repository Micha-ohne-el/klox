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
            if (match(Fun)) {
                return parseFunDeclaration()
            }

            if (match(Var)) {
                return parseVarDeclaration()
            }

            return parseStatement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun parseFunDeclaration(): Statement {
        return parseFunction(kind = "function")
    }

    private fun parseFunction(kind: String): FunctionStatement {
        val name = consume(Identifier, "Expecting $kind name.")

        consume(LeftParen, "Expecting '(' after $kind name.")

        val parameters = mutableListOf<Token>()

        if (!check(RightParen)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }

                parameters.add(consume(Identifier, "Expecting parameter name."))
            } while (match(Comma))
        }

        consume(RightParen, "Expecting ')' after parameters.")

        consume(LeftBrace, "Expecting '{' before $kind body.")

        val body = parseBlockStatement()

        return FunctionStatement(name, parameters, body)
    }

    private fun parseVarDeclaration(): Statement {
        val name = consume(Identifier, "Expecting variable name.")

        val initializer = if (match(Equal)) {
            parseExpression()
        } else {
            null
        }

        consume(Semicolon, "Expecting ';' after variable declaration.")

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

        if (match(Return)) {
            return parseReturnStatement()
        }

        if (match(LeftBrace)) {
            return BlockStatement(parseBlockStatement())
        }

        return parseExpressionStatement()
    }

    private fun parseIfStatement(): Statement {
        consume(LeftParen, "Expecting '(' after 'if'.")

        val condition = parseExpression()

        consume(RightParen, "Expecting ')' after condition.")

        val thenBranch = parseStatement()

        val elseBranch = if (match(Else)) {
            parseStatement()
        } else {
            null
        }

        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Statement {
        consume(LeftParen, "Expecting '(' after 'while'.")

        val condition = parseExpression()

        consume(RightParen, "Expecting ')' after condition.")

        val statement = parseStatement()

        return WhileStatement(condition, statement)
    }

    private fun parseForStatement(): Statement {
        consume(LeftParen, "Expecting '(' after 'for'.")

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

        consume(Semicolon, "Expecting ';' after condition.")

        val increment = if (!check(RightParen)) {
            parseExpression()
        } else {
            null
        }

        consume(RightParen, "Expecting ')' after for clauses.")

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

        consume(Semicolon, "Expecting ';' after statement.")

        return PrintStatement(expression)
    }

    private fun parseReturnStatement(): Statement {
        val keyword = previous

        val value = if (!check(Semicolon)) {
            parseExpression()
        } else {
            null
        }

        consume(Semicolon, "Expecting ';' after return value.")

        return ReturnStatement(keyword, value)
    }

    private fun parseBlockStatement(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!check(RightBrace) && !isAtEnd) {
            statements.add(parseDeclaration() ?: continue)
        }

        consume(RightBrace, "Expecting '}' after block.")

        return statements
    }

    private fun parseExpressionStatement(): Statement {
        val expression = parseExpression()

        consume(Semicolon, "Expecting ';' after expression.")

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

        return parseCall()
    }

    private fun parseCall(): Expression {
        var expression = parsePrimary()

        while (true) {
            if (match(LeftParen)) {
                expression = finishCall(expression)
            } else {
                break
            }
        }

        return expression
    }

    private fun finishCall(callee: Expression): Expression {
        val arguments = mutableListOf<Expression>()

        if (!check(RightParen)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }

                arguments.add(parseExpression())
            } while (match(Comma))
        }

        val paren = consume(RightParen, "Expecting ')' after arguments.")

        return CallExpression(callee, paren, arguments)
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
            consume(RightParen, "Expecting ')' after expression.")

            return GroupingExpression(expression)
        }

        throw error(peek(), "Expecting expression.")
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

                else -> {}
            }

            advance()
        }
    }

    private class ParseError : RuntimeException()
}
