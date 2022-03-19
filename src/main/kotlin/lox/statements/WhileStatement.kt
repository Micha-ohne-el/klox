package lox.statements

import lox.expressions.Expression

class WhileStatement(
    val condition: Expression,
    val body: Statement
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
