package lox

import lox.TokenType.*
import lox.expressions.*
import lox.statements.*

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    val globals = Environment()

    fun interpret(statements: List<Statement>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            error(error)
        }
    }

    fun execute(statement: Statement) {
        statement.accept(this)
    }

    fun executeBlock(statements: List<Statement>, newEnvironment: Environment) {
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

    fun resolve(expression: Expression, depth: Int) {
        locals[expression] = depth
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

    override fun visit(callExpression: CallExpression): Any? {
        val callee = evaluate(callExpression.callee)

        val arguments = callExpression.arguments.map(::evaluate)

        if (callee !is Callable) {
            throw RuntimeError(callExpression.paren, "Can only call functions and classes.")
        }

        if (arguments.size != callee.arity) {
            throw RuntimeError(
                callExpression.paren,
                "Expected ${callee.arity} arguments but got ${arguments.size}."
            )
        }

        return callee.call(this, arguments)
    }

    override fun visit(groupingExpression: GroupingExpression): Any? {
        return evaluate(groupingExpression.expression)
    }

    override fun visit(variableExpression: VariableExpression): Any? {
        return lookUpVariable(variableExpression.name, variableExpression)
    }

    override fun visit(assignmentExpression: AssignmentExpression): Any? {
        val value = evaluate(assignmentExpression.value)

        val distance = locals[assignmentExpression]

        if (distance != null) {
            environment.assignAt(distance, assignmentExpression.name, value)
        } else {
            globals.assign(assignmentExpression.name, value)
        }

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

    override fun visit(functionStatement: FunctionStatement) {
        val function = Function(functionStatement, environment)

        environment.define(functionStatement.name.lexeme, function)
    }

    override fun visit(returnStatement: ReturnStatement) {
        val value = if (returnStatement.value != null) {
            evaluate(returnStatement.value)
        } else {
            null
        }

        throw Return(value)
    }


    private var environment = globals
    private val locals = mutableMapOf<Expression, Int>()

    init {
        globals.define("clock", Clock())
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

    private fun lookUpVariable(name: Token, expression: Expression): Any? {
        val distance = locals[expression]

        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }
}
