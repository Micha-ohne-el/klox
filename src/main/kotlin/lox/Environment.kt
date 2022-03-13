package lox

class Environment(
    private val parent: Environment? = null
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


    private val values = mutableMapOf<String, Any?>()
}
