package lox.statements

import lox.Token

open class VariableStatement(
    val name: Token,
    val initializer: lox.expressions.Expression?
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
