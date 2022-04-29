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

    override fun visit(assignmentExpression: AssignmentExpression) {
        resolve(assignmentExpression.value)
        resolveLocal(assignmentExpression, assignmentExpression.name)
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

    override fun visit(getExpression: GetExpression) {
        resolve(getExpression.target)
    }

    override fun visit(groupingExpression: GroupingExpression) {
        resolve(groupingExpression.expression)
    }

    override fun visit(literalExpression: LiteralExpression) {}

    override fun visit(prefixExpression: PrefixExpression) {
        resolve(prefixExpression.right)
    }

    override fun visit(setExpression: SetExpression) {
        resolve(setExpression.value)
        resolve(setExpression.target)
    }

    override fun visit(shortingExpression: ShortingExpression) {
        resolve(shortingExpression.left)
        resolve(shortingExpression.right)
    }

    override fun visit(superExpression: SuperExpression) {
        if (currentClassType == ClassType.None) {
            error(superExpression.keyword, "Can't use 'super' outside of a class.")
        } else if (currentClassType == ClassType.Class) {
            error(superExpression.keyword, "Can't use 'super' in a class with no superclass.")
        }

        resolveLocal(superExpression, superExpression.keyword)
    }

    override fun visit(thisExpression: ThisExpression) {
        if (currentClassType == ClassType.None) {
            error(thisExpression.keyword, "Can't use 'this' outside of a class.")
            return
        }

        resolveLocal(thisExpression, thisExpression.keyword)
    }

    override fun visit(variableExpression: VariableExpression) {
        if (scopes.isNotEmpty() && scopes.peek()[variableExpression.name.lexeme] == false) {
            error(variableExpression.name, "Can't read local variable in its own initializer.")
        }

        resolveLocal(variableExpression, variableExpression.name)
    }

    override fun visit(blockStatement: BlockStatement) {
        scoped {
            resolve(blockStatement.statements)
        }
    }

    override fun visit(classStatement: ClassStatement) {
        val enclosingClassType = currentClassType
        currentClassType = ClassType.Class

        declare(classStatement.name)
        define(classStatement.name)

        if (classStatement.superclass != null) {
            if (classStatement.name.lexeme == classStatement.superclass.name.lexeme) {
                error(classStatement.superclass.name, "A class can't inherit from itself.")
            }

            currentClassType = ClassType.Subclass

            resolve(classStatement.superclass)

            scopes.push(mutableMapOf())
            scopes.peek()["super"] = true
        }

        scoped {
            scopes.peek()["this"] = true

            for (method in classStatement.methods) {
                if (method.name.lexeme == "init") {
                    resolveFunction(method, FunctionType.Initializer)
                } else {
                    resolveFunction(method, FunctionType.Method)
                }
            }
        }

        if (classStatement.superclass != null) {
            scopes.pop()
        }

        currentClassType = enclosingClassType
    }

    override fun visit(expressionStatement: ExpressionStatement) {
        resolve(expressionStatement.expression)
    }

    override fun visit(functionStatement: FunctionStatement) {
        declare(functionStatement.name)
        define(functionStatement.name)

        resolveFunction(functionStatement, FunctionType.Function)
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
            if (currentFunctionType == FunctionType.Initializer) {
                error(returnStatement.keyword, "Can't return a value from an initializer.")
            }

            resolve(returnStatement.value)
        }
    }

    override fun visit(variableStatement: VariableStatement) {
        declare(variableStatement.name)

        if (variableStatement.initializer != null) {
            resolve(variableStatement.initializer)
        }

        define(variableStatement.name)
    }

    override fun visit(whileStatement: WhileStatement) {
        resolve(whileStatement.condition)

        resolve(whileStatement.body)
    }


    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunctionType = FunctionType.None
    private var currentClassType = ClassType.None

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
        Function,
        Method,
        Initializer
    }

    private enum class ClassType {
        None,
        Class,
        Subclass
    }
}
