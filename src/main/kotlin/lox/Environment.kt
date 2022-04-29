package lox

class Environment(
    val parent: Environment? = null
) {
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (name.lexeme in values) {
            return values[name.lexeme]
        }

        if (parent != null) {
            return parent.get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun getAt(distance: Int, name: String): Any? {
        return getAncestor(distance).values[name]
    }

    fun assign(name: Token, value: Any?) {
        if (name.lexeme in values) {
            values[name.lexeme] = value
            return
        }

        if (parent != null) {
            parent.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        getAncestor(distance).values[name.lexeme] = value
    }


    private val values = mutableMapOf<String, Any?>()

    private fun getAncestor(distance: Int): Environment {
        var environment = this

        for (i in 1..distance) {
            environment = environment.parent!!
        }

        return environment
    }
}
