package lox.statements

import lox.Token
import lox.expressions.Expression

class VariableStatement(
    val name: Token,
    val initializer: Expression?
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
