package lox

interface Callable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?

    val arity: Int
}
