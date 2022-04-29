package lox.expressions

import lox.Token

class SuperExpression(
    val keyword: Token,
    val method: Token
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
