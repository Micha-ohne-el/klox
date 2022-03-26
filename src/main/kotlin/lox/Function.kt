package lox

import lox.statements.FunctionStatement

class Function(
    private val declaration: FunctionStatement,
    private val closure: Environment
) : Callable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (index in declaration.params.indices) {
            environment.define(declaration.params[index].lexeme, arguments[index])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (output: Return) {
            return output.value
        }

        return null
    }

    override val arity = declaration.params.size

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}
