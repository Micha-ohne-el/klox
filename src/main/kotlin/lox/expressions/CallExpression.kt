package lox.expressions

import lox.Token

class CallExpression(
    val callee: Expression,
    val paren: Token,
    val arguments: List<Expression>
) : Expression() {
    override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
}
