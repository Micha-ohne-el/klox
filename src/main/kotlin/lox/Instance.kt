package lox

class Instance(
    private val loxClass: Class
) {
    fun get(name: Token): Any? {
        return fields.getOrElse(name.lexeme) {
            val method = loxClass.findMethod(name.lexeme)

            if (method != null) {
                return method.bind(this)
            }

            throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
        }
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString() = "${loxClass.name} instance"


    private val fields = mutableMapOf<String, Any?>()
}
