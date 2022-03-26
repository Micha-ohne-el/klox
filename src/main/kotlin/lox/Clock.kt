package lox

class Clock : Callable {
    override val arity = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Double {
        return System.currentTimeMillis() / 1000.0
    }

    override fun toString() = "<native fn>"
}
