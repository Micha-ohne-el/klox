package lox.statements

import lox.expressions.Expression

class PrintStatement(
    val expression: Expression
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
