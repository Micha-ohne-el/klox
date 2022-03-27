package lox

import lox.expressions.*
import lox.statements.*
import java.util.Stack

class Resolver(
    private val interpreter: Interpreter
) : Expression.Visitor<Unit>, Statement.Visitor<Unit> {
    fun resolve(statements: List<Statement>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    override fun visit(variableExpression: VariableExpression) {
        if (scopes.isNotEmpty() && scopes.peek()[variableExpression.name.lexeme] == false) {
            error(variableExpression.name, "Can't read local variable in its own initializer.")
        }

        resolveLocal(variableExpression, variableExpression.name)
    }

    override fun visit(assignmentExpression: AssignmentExpression) {
        resolve(assignmentExpression.value)
        resolveLocal(assignmentExpression, assignmentExpression.name)
    }

    override fun visit(variableStatement: VariableStatement) {
        declare(variableStatement.name)

        if (variableStatement.initializer != null) {
            resolve(variableStatement.initializer)
        }

        define(variableStatement.name)
    }

    override fun visit(blockStatement: BlockStatement) {
        scoped {
            resolve(blockStatement.statements)
        }
    }

    override fun visit(functionStatement: FunctionStatement) {
        declare(functionStatement.name)
        define(functionStatement.name)

        resolveFunction(functionStatement, FunctionType.Function)
    }

    override fun visit(expressionStatement: ExpressionStatement) {
        resolve(expressionStatement.expression)
    }

    override fun visit(ifStatement: IfStatement) {
        resolve(ifStatement.condition)

        resolve(ifStatement.thenBranch)

        if (ifStatement.elseBranch != null) {
            resolve(ifStatement.elseBranch)
        }
    }

    override fun visit(printStatement: PrintStatement) {
        resolve(printStatement.expression)
    }

    override fun visit(returnStatement: ReturnStatement) {
        if (currentFunctionType == FunctionType.None) {
            error(returnStatement.keyword, "Can't return from top-level code.")
        }

        if (returnStatement.value != null) {
            resolve(returnStatement.value)
        }
    }

    override fun visit(whileStatement: WhileStatement) {
        resolve(whileStatement.condition)

        resolve(whileStatement.body)
    }

    override fun visit(binaryExpression: BinaryExpression) {
        resolve(binaryExpression.left)

        resolve(binaryExpression.right)
    }

    override fun visit(callExpression: CallExpression) {
        resolve(callExpression.callee)

        for (argument in callExpression.arguments) {
            resolve(argument)
        }
    }

    override fun visit(groupingExpression: GroupingExpression) {
        resolve(groupingExpression.expression)
    }

    override fun visit(literalExpression: LiteralExpression) {}

    override fun visit(shortingExpression: ShortingExpression) {
        resolve(shortingExpression.left)
        resolve(shortingExpression.right)
    }

    override fun visit(prefixExpression: PrefixExpression) {
        resolve(prefixExpression.right)
    }


    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunctionType = FunctionType.None

    private fun scoped(block: () -> Unit) {
        scopes.push(mutableMapOf())

        block()

        scopes.pop()
    }

    private fun resolve(statement: Statement) {
        statement.accept(this)
    }

    private fun resolve(expression: Expression) {
        expression.accept(this)
    }

    // TODO: Remove second parameter?
    private fun resolveLocal(expression: Expression, name: Token) {
        for (index in scopes.indices.reversed()) {
            if (name.lexeme in scopes[index]) {
                interpreter.resolve(expression, scopes.size - 1 - index)

                return
            }
        }
    }

    private fun resolveFunction(functionStatement: FunctionStatement, type: FunctionType) {
        val enclosingFunctionType = currentFunctionType
        currentFunctionType = type

        scoped {
            for (param in functionStatement.params) {
                declare(param)
                define(param)
            }

            resolve(functionStatement.body)
        }

        currentFunctionType = enclosingFunctionType
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) {
            return
        }

        val scope = scopes.peek()

        if (name.lexeme in scope) {
            error(name, "Already a variable with this name in this scope.")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) {
            return
        }

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }

    private enum class FunctionType {
        None,
        Function
    }
}
