package lox.expressions

import lox.Token

class SetExpression(
    val target: Expression,
    val name: Token,
    val value: Expression
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
