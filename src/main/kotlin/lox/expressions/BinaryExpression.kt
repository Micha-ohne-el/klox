package lox.expressions

import lox.Token

class BinaryExpression(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
