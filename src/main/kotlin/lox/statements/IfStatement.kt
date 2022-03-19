package lox.statements

import lox.expressions.Expression

class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
