package lox.statements

import lox.Token
import lox.expressions.Expression

class ReturnStatement(
    val keyword: Token,
    val value: Expression?
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
