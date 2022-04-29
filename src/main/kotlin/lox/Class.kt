package lox

class Class(
    val name: String,
    private val superclass: Class?,
    private val methods: Map<String, Function>
) : Callable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Instance {
        val instance = Instance(this)

        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }

    override val arity: Int get() {
        return findMethod("init")?.arity ?: 0
    }

    fun findMethod(name: String): Function? {
        return methods[name] ?: superclass?.findMethod(name)
    }

    override fun toString() = name
}
