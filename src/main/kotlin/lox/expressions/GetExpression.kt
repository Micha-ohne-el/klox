package lox.expressions

import lox.Token

class GetExpression(
    val target: Expression,
    val name: Token
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
