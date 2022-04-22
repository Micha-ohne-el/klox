package lox

import lox.statements.FunctionStatement

class Function(
    private val declaration: FunctionStatement,
    private val closure: Environment,
    private val isInitializer: Boolean
) : Callable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (index in declaration.params.indices) {
            environment.define(declaration.params[index].lexeme, arguments[index])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (output: Return) {
            if (isInitializer) {
                return closure.getAt(0, "this")
            }

            return output.value
        }

        if (isInitializer) {
            return closure.getAt(0, "this")
        }

        return null
    }

    override val arity = declaration.params.size

    fun bind(instance: Instance): Function {
        val environment = Environment(closure)

        environment.define("this", instance)

        return Function(declaration, environment, isInitializer)
    }

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}
