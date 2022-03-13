package lox

import lox.TokenType.*

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    fun interpret(statements: List<Statement>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            error(error)
        }
    }

    override fun visit(literalExpression: Expression.Literal): Any? {
        return literalExpression.value
    }

    override fun visit(prefixExpression: Expression.Prefix): Any? {
        val right = evaluate(prefixExpression.right)

        return when (prefixExpression.operator.type) {
            Minus -> -(right as Double)
            Bang -> !isTruthy(right)

            else -> null
        }
    }

    override fun visit(binaryExpression: Expression.Binary): Any? {
        val left = evaluate(binaryExpression.left)
        val right = evaluate(binaryExpression.right)

        return when (binaryExpression.operator.type) {
            Minus -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) - (right as Double)
            }
            Slash -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) / (right as Double)
            }
            Asterisk -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) * (right as Double)
            }

            Plus -> {
                if (left is Double && right is Double) {
                    return left + right
                }
                if (left is String && right is String) {
                    return left + right
                }
                throw RuntimeError(binaryExpression.operator, "Operands must both be numbers or strings.")
            }

            Greater -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) > (right as Double)
            }
            GreaterEqual -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) >= (right as Double)
            }
            Less -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) < (right as Double)
            }
            LessEqual -> {
                checkNumberOperands(binaryExpression.operator, left, right)
                (left as Double) <= (right as Double)
            }

            BangEqual -> left != right
            EqualEqual -> left == right

            else -> null
        }
    }

    override fun visit(groupingExpression: Expression.Grouping): Any? {
        return evaluate(groupingExpression.expression)
    }

    override fun visit(variableExpression: Expression.Variable): Any? {
        return environment.get(variableExpression.name)
    }

    override fun visit(assignmentExpression: Expression.Assignment): Any? {
        val value = evaluate(assignmentExpression.value)

        environment.assign(assignmentExpression.name, value)

        return value
    }

    override fun visit(expressionStatement: Statement.Expression) {
        evaluate(expressionStatement.expression)
    }

    override fun visit(printStatement: Statement.Print) {
        val expression = evaluate(printStatement.expression)

        println(stringify(expression))
    }

    override fun visit(variableStatement: Statement.Variable) {
        val value = if (variableStatement.initializer != null) {
            evaluate(variableStatement.initializer)
        } else {
            null
        }

        environment.define(variableStatement.name.lexeme, value)
    }

    override fun visit(blockStatement: Statement.Block) {
        executeBlock(blockStatement.statements, Environment(environment))
    }


    private var environment = Environment()

    private fun execute(statement: Statement) {
        statement.accept(this)
    }

    private fun executeBlock(statements: List<Statement>, newEnvironment: Environment) {
        val previousEnvironment = environment

        try {
            environment = newEnvironment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            environment = previousEnvironment
        }
    }

    private fun evaluate(expression: Expression): Any? {
        return expression.accept(this)
    }

    private fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value

            else -> false
        }
    }

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (!operands.all {operand -> operand is Double}) {
            throw RuntimeError(
                operator,
                if (operands.size == 1) {"Operand must be a number."}
                else {"Operands must be numbers."}
            )
        }
    }

    private fun stringify(value: Any?): String {
        if (value == null) {return "nil"}

        if (value is Double) {
            val string = value.toString()
            if (string.endsWith(".0")) {
                return string.substring(0, string.length - 2)
            }
            return string
        }

        return value.toString()
    }
}
