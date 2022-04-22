package lox.expressions

import lox.Token

class ThisExpression(
    val keyword: Token
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
