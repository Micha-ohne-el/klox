package lox.statements

import lox.Token
import lox.expressions.VariableExpression

class ClassStatement(
    val name: Token,
    val superclass: VariableExpression?,
    val methods: List<FunctionStatement>
) : Statement() {
    override fun accept(visitor: Visitor<Unit>) = visitor.visit(this)
}
