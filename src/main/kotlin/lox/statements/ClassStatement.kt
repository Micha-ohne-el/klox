package lox.statements

import lox.Token

open class ClassStatement(
    val name: Token,
    val methods: List<FunctionStatement>
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
