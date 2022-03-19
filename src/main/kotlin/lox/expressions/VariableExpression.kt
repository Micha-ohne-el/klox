package lox.expressions

import lox.Token

class VariableExpression(
    val name: Token
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
