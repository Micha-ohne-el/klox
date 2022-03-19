package lox

import lox.TokenType.*
import lox.expressions.*
import lox.statements.*

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

    override fun visit(literalExpression: LiteralExpression): Any? {
        return literalExpression.value
    }

    override fun visit(prefixExpression: PrefixExpression): Any? {
        val right = evaluate(prefixExpression.right)

        return when (prefixExpression.operator.type) {
            Minus -> -(right as Double)
            Bang -> !isTruthy(right)

            else -> null
        }
    }

    override fun visit(binaryExpression: BinaryExpression): Any? {
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

    override fun visit(groupingExpression: GroupingExpression): Any? {
        return evaluate(groupingExpression.expression)
    }

    override fun visit(variableExpression: VariableExpression): Any? {
        return environment.get(variableExpression.name)
    }

    override fun visit(assignmentExpression: AssignmentExpression): Any? {
        val value = evaluate(assignmentExpression.value)

        environment.assign(assignmentExpression.name, value)

        return value
    }

    override fun visit(shortingExpression: ShortingExpression): Any? {
        val left = evaluate(shortingExpression.left)

        if (shortingExpression.operator.type == Or) {
            if (isTruthy(left)) {return left}
        } else {
            if (!isTruthy(left)) {return left}
        }

        return evaluate(shortingExpression.right)
    }

    override fun visit(expressionStatement: ExpressionStatement) {
        evaluate(expressionStatement.expression)
    }

    override fun visit(printStatement: PrintStatement) {
        val expression = evaluate(printStatement.expression)

        println(stringify(expression))
    }

    override fun visit(variableStatement: VariableStatement) {
        val value = if (variableStatement.initializer != null) {
            evaluate(variableStatement.initializer)
        } else {
            null
        }

        environment.define(variableStatement.name.lexeme, value)
    }

    override fun visit(blockStatement: BlockStatement) {
        executeBlock(blockStatement.statements, Environment(environment))
    }

    override fun visit(ifStatement: IfStatement) {
        val condition = evaluate(ifStatement.condition)

        if (isTruthy(condition)) {
            execute(ifStatement.thenBranch)
        } else if (ifStatement.elseBranch != null) {
            execute(ifStatement.elseBranch)
        }
    }

    override fun visit(whileStatement: WhileStatement) {
        while (isTruthy(evaluate(whileStatement.condition))) {
            execute(whileStatement.body)
        }
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

            else -> true
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
